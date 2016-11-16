#pragma once

#include <mbgl/map/map.hpp>
#include <mbgl/map/view.hpp>
#include <mbgl/map/backend.hpp>
#include <mbgl/util/noncopyable.hpp>
#include <mbgl/platform/default/thread_pool.hpp>
#include <mbgl/storage/default_file_source.hpp>

#include <string>
#include <jni.h>
#include <android/native_window.h>
#include <EGL/egl.h>

namespace mbgl {
namespace android {

class NativeMapView : public mbgl::View, public mbgl::Backend {
public:
    NativeMapView(JNIEnv *env, jobject obj, float pixelRatio, int availableProcessors, size_t totalMemory);
    virtual ~NativeMapView();

    mbgl::Size getFramebufferSize() const;
    void bind() override;

    void activate() override;
    void deactivate() override;
    void invalidate() override;

    void notifyMapChange(mbgl::MapChange) override;

    mbgl::Map &getMap();
    mbgl::DefaultFileSource &getFileSource();

    void initializeDisplay();
    void terminateDisplay();

    void initializeContext();
    void terminateContext();

    void createSurface(ANativeWindow *window);
    void destroySurface();

    void render();

    void enableFps(bool enable);
    void updateFps();

    void resizeView(int width, int height);
    void resizeFramebuffer(int width, int height);
    mbgl::EdgeInsets getInsets() { return insets;}
    void setInsets(mbgl::EdgeInsets insets_);

    void scheduleTakeSnapshot();

private:
    EGLConfig chooseConfig(const EGLConfig configs[], EGLint numConfigs);

private:
    JavaVM *vm = nullptr;
    JNIEnv *env = nullptr;
    jweak obj = nullptr;

    ANativeWindow *window = nullptr;

    std::string styleUrl;
    std::string apiKey;

    bool fpsEnabled = false;
    bool snapshot = false;
    double fps = 0.0;

    int width = 0;
    int height = 0;
    int fbWidth = 0;
    int fbHeight = 0;
    bool framebufferSizeChanged = true;

    int availableProcessors = 0;
    size_t totalMemory = 0;

    // Ensure these are initialised last
    std::unique_ptr<mbgl::DefaultFileSource> fileSource;
    mbgl::ThreadPool threadPool;
    std::unique_ptr<mbgl::Map> map;
    mbgl::EdgeInsets insets;

};
}
}
