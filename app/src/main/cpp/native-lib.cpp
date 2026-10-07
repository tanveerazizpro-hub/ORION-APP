#include <jni.h>
#include <string>
#include <vector>
#include <fstream>
#include <algorithm>
#include <mutex>
#include <atomic>
#include <ctime>
#include <cstdio>
#include <cstdlib>
#include <android/log.h>
#include "llama.h"

#define TAG "OrionNative"

static llama_model*   g_model = nullptr;
static llama_context* g_ctx   = nullptr;
static std::string    g_loadedPath = "";
static int            g_loadedCtx  = 0;
static std::atomic<int> g_loadProgress{-1};
static std::mutex g_loadMutex;

static std::mutex g_logMutex;
static std::string g_logPath;

static void writeLogLine(const char* level, const char* text) {
    std::lock_guard<std::mutex> lock(g_logMutex);
    if (g_logPath.empty()) return;
    FILE* f = fopen(g_logPath.c_str(), "a");
    if (!f) return;
    time_t now = time(nullptr);
    struct tm* t = localtime(&now);
    char timeBuf[32];
    strftime(timeBuf, sizeof(timeBuf), "%H:%M:%S", t);
    std::string s(text);
    while (!s.empty() && (s.back() == '\n' || s.back() == '\r')) s.pop_back();
    fprintf(f, "[%s] [%s] %s\n", timeBuf, level, s.c_str());
    fflush(f);
    fclose(f);
}

static void parseProgress(const char* text) {
    std::string s(text);
    size_t pctPos = s.rfind('%');
    if (pctPos == std::string::npos || pctPos == 0) return;
    size_t i = pctPos;
    while (i > 0 && (isdigit((unsigned char)s[i-1]) || s[i-1] == '.')) i--;
    if (i == pctPos) return;
    try {
        float v = std::stof(s.substr(i, pctPos - i));
        if (v >= 0.0f && v <= 100.0f) g_loadProgress.store((int)v);
    } catch (...) {}
}

static void llamaLogCallback(ggml_log_level level, const char* text, void*) {
    const char* lvl = "INFO";
    switch (level) {
        case GGML_LOG_LEVEL_ERROR: lvl = "ERROR"; break;
        case GGML_LOG_LEVEL_WARN:  lvl = "WARN";  break;
        case GGML_LOG_LEVEL_DEBUG: lvl = "DEBUG"; break;
        case GGML_LOG_LEVEL_CONT:  lvl = "CONT";  break;
        default: break;
    }
    if (level == GGML_LOG_LEVEL_INFO || level == GGML_LOG_LEVEL_CONT) parseProgress(text);
    writeLogLine(lvl, text);
}

extern "C" JNIEXPORT void JNICALL
Java_com_orion_app_LlamaEngine_setLogFile(JNIEnv* env, jobject, jstring path) {
    const char* p = env->GetStringUTFChars(path, nullptr);
    {
        std::lock_guard<std::mutex> lock(g_logMutex);
        g_logPath = p;
        FILE* f = fopen(g_logPath.c_str(), "w");
        if (f) fclose(f);
    }
    env->ReleaseStringUTFChars(path, p);
    llama_log_set(llamaLogCallback, nullptr);
    writeLogLine("INFO", "O.R.I.O.N. diagnostic log started");
}

extern "C" JNIEXPORT jint JNICALL
Java_com_orion_app_LlamaEngine_getLoadProgress(JNIEnv*, jobject) {
    return g_loadProgress.load();
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_orion_app_LlamaEngine_isModelLoaded(JNIEnv* env, jobject, jstring path, jint nCtx) {
    const char* p = env->GetStringUTFChars(path, nullptr);
    std::string s(p);
    env->ReleaseStringUTFChars(path, p);
    std::lock_guard<std::mutex> lock(g_loadMutex);
    return (g_model != nullptr && g_ctx != nullptr &&
            s == g_loadedPath && nCtx == g_loadedCtx) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orion_app_LlamaEngine_getLogTail(JNIEnv* env, jobject, jint maxLines) {
    std::lock_guard<std::mutex> lock(g_logMutex);
    if (g_logPath.empty()) return env->NewStringUTF("(log not initialized)");
    FILE* f = fopen(g_logPath.c_str(), "r");
    if (!f) return env->NewStringUTF("(log file not found)");
    std::string content;
    char buf[4096];
    size_t n;
    while ((n = fread(buf, 1, sizeof(buf), f)) > 0) content.append(buf, n);
    fclose(f);
    std::vector<std::string> lines;
    size_t start = 0;
    while (start < content.size()) {
        size_t end = content.find('\n', start);
        if (end == std::string::npos) { lines.push_back(content.substr(start)); break; }
        lines.push_back(content.substr(start, end - start));
        start = end + 1;
    }
    int begin = std::max(0, (int)lines.size() - maxLines);
    std::string result;
    for (int i = begin; i < (int)lines.size(); i++) { result += lines[i]; result += "\n"; }
    return env->NewStringUTF(result.c_str());
}

static void freeInternal() {
    if (g_ctx)   { llama_free(g_ctx); g_ctx = nullptr; }
    if (g_model) { llama_model_free(g_model); g_model = nullptr; }
    g_loadedPath = "";
    g_loadedCtx = 0;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_orion_app_LlamaEngine_loadModel(JNIEnv* env, jobject, jstring modelPath, jint nCtx) {
    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    std::string pathStr(path);
    env->ReleaseStringUTFChars(modelPath, path);

    std::lock_guard<std::mutex> lock(g_loadMutex);

    if (g_model && g_ctx && pathStr == g_loadedPath && nCtx == g_loadedCtx) {
        writeLogLine("INFO", "loadModel: same model+ctx already loaded, skipping");
        g_loadProgress.store(100);
        return JNI_TRUE;
    }

    g_loadProgress.store(0);
    writeLogLine("INFO", "loadModel called (no pinning)");

    freeInternal();

    llama_backend_init();

    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0;

    g_model = llama_model_load_from_file(pathStr.c_str(), model_params);

    if (!g_model) {
        g_loadProgress.store(-1);
        return JNI_FALSE;
    }

    // 3 threads, no pinning — matches best unpinned Termux benchmark (13.34 tok/s)
    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx           = nCtx;
    ctx_params.n_threads       = 3;
    ctx_params.n_threads_batch = 3;

    g_ctx = llama_init_from_model(g_model, ctx_params);
    if (!g_ctx) {
        llama_model_free(g_model);
        g_model = nullptr;
        g_loadProgress.store(-1);
        return JNI_FALSE;
    }

    g_loadedPath = pathStr;
    g_loadedCtx  = nCtx;
    g_loadProgress.store(100);

    char msg[128];
    snprintf(msg, sizeof(msg), "Model loaded OK, n_threads=3, n_ctx=%d", nCtx);
    writeLogLine("INFO", msg);
    return JNI_TRUE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orion_app_LlamaEngine_generateStreaming(
        JNIEnv* env, jobject,
        jstring prompt, jint maxTokens, jfloat temp, jint threads,
        jobject callback) {

    if (!g_model || !g_ctx) return env->NewStringUTF("");

    jclass cbClass = env->GetObjectClass(callback);
    jmethodID onTokenMethod = env->GetMethodID(cbClass, "onToken", "(Ljava/lang/String;)V");
    if (onTokenMethod == nullptr) return env->NewStringUTF("");

    const char* p = env->GetStringUTFChars(prompt, nullptr);
    std::string prompt_text(p);
    env->ReleaseStringUTFChars(prompt, p);

    const llama_vocab* vocab = llama_model_get_vocab(g_model);

    llama_memory_clear(llama_get_memory(g_ctx), true);

    int n_prompt = -llama_tokenize(vocab, prompt_text.c_str(), prompt_text.size(),
                                   nullptr, 0, true, true);
    std::vector<llama_token> tokens(n_prompt);
    llama_tokenize(vocab, prompt_text.c_str(), prompt_text.size(),
                   tokens.data(), tokens.size(), true, true);

    llama_batch batch = llama_batch_get_one(tokens.data(), tokens.size());
    if (llama_decode(g_ctx, batch)) return env->NewStringUTF("");

    auto sparams = llama_sampler_chain_default_params();
    llama_sampler* smpl = llama_sampler_chain_init(sparams);
    llama_sampler_chain_add(smpl, llama_sampler_init_temp(temp));
    llama_sampler_chain_add(smpl, llama_sampler_init_dist(LLAMA_DEFAULT_SEED));

    std::string result;
    for (int i = 0; i < maxTokens; i++) {
        llama_token tok = llama_sampler_sample(smpl, g_ctx, -1);
        if (llama_vocab_is_eog(vocab, tok)) break;

        char buf[256];
        int n = llama_token_to_piece(vocab, tok, buf, sizeof(buf), 0, true);
        if (n > 0) {
            std::string piece(buf, n);
            result += piece;
            jstring jpiece = env->NewStringUTF(piece.c_str());
            if (jpiece != nullptr) {
                env->CallVoidMethod(callback, onTokenMethod, jpiece);
                env->DeleteLocalRef(jpiece);
            }
        }
        llama_batch nb = llama_batch_get_one(&tok, 1);
        if (llama_decode(g_ctx, nb)) break;
    }

    llama_sampler_free(smpl);
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_orion_app_LlamaEngine_freeModel(JNIEnv*, jobject) {
    std::lock_guard<std::mutex> lock(g_loadMutex);
    freeInternal();
    llama_backend_free();
    g_loadProgress.store(-1);
}
