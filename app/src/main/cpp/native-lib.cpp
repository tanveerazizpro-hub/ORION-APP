#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include "llama.h"

#define TAG "OrionNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static llama_model*   g_model = nullptr;
static llama_context* g_ctx   = nullptr;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_orion_app_LlamaEngine_loadModel(JNIEnv* env, jobject, jstring modelPath) {
    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    LOGI("Loading model: %s", path);

    llama_backend_init();

    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0;      // CPU only (Mali Vulkan broken)

    g_model = llama_model_load_from_file(path, model_params);
    env->ReleaseStringUTFChars(modelPath, path);

    if (!g_model) {
        LOGE("Failed to load model");
        return JNI_FALSE;
    }

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx           = 4096;
    ctx_params.n_threads       = 4;     // A76 cluster only
    ctx_params.n_threads_batch = 4;

    g_ctx = llama_init_from_model(g_model, ctx_params);
    if (!g_ctx) {
        LOGE("Failed to create context");
        llama_model_free(g_model);
        g_model = nullptr;
        return JNI_FALSE;
    }

    LOGI("Model loaded OK");
    return JNI_TRUE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_orion_app_LlamaEngine_generate(JNIEnv* env, jobject, jstring prompt,
                                        jint maxTokens, jfloat temp, jint threads) {
    if (!g_model || !g_ctx) {
        LOGE("Model not loaded");
        return env->NewStringUTF("");
    }

    const char* p = env->GetStringUTFChars(prompt, nullptr);
    std::string prompt_text(p);
    env->ReleaseStringUTFChars(prompt, p);

    const llama_vocab* vocab = llama_model_get_vocab(g_model);

    // Tokenize the prompt
    int n_prompt = -llama_tokenize(vocab, prompt_text.c_str(), prompt_text.size(),
                                   nullptr, 0, true, true);
    std::vector<llama_token> tokens(n_prompt);
    llama_tokenize(vocab, prompt_text.c_str(), prompt_text.size(),
                   tokens.data(), tokens.size(), true, true);

    // Feed prompt to context
    llama_batch batch = llama_batch_get_one(tokens.data(), tokens.size());
    if (llama_decode(g_ctx, batch)) {
        LOGE("Failed to decode prompt");
        return env->NewStringUTF("");
    }

    // Sampler chain
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
        if (n > 0) result.append(buf, n);

        llama_batch nb = llama_batch_get_one(&tok, 1);
        if (llama_decode(g_ctx, nb)) break;
    }

    llama_sampler_free(smpl);
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_orion_app_LlamaEngine_freeModel(JNIEnv*, jobject) {
    if (g_ctx)   { llama_free(g_ctx); g_ctx = nullptr; }
    if (g_model) { llama_model_free(g_model); g_model = nullptr; }
    llama_backend_free();
}
