
#include "Includes/obfuscate.h"
#include "Menu/Menu.hpp"

bool iconValid = false;
bool settingsValid = false;
bool featuresValid = false;
bool initValid = false;

//Big letter cause crash
void setText(JNIEnv *env, jobject obj, const char* text){
    //https://stackoverflow.com/a/33627640/3763113
    //A little JNI calls here. You really really need a great knowledge if you want to play with JNI stuff
    //Html.fromHtml("");
    jclass html = (*env).FindClass(OBFUSCATE("android/text/Html"));
    jmethodID fromHtml = (*env).GetStaticMethodID(html, OBFUSCATE("fromHtml"), OBFUSCATE("(Ljava/lang/String;)Landroid/text/Spanned;"));

    //setText("");
    jclass textView = (*env).FindClass(OBFUSCATE("android/widget/TextView"));
    jmethodID setText = (*env).GetMethodID(textView, OBFUSCATE("setText"), OBFUSCATE("(Ljava/lang/CharSequence;)V"));

    //Java string
    jstring jstr = (*env).NewStringUTF(text);
    (*env).CallVoidMethod(obj, setText,  (*env).CallStaticObjectMethod(html, fromHtml, jstr));
}

jstring Icon(JNIEnv *env, jobject thiz) {

    //Use https://www.base64encode.org/ to encode your image to base64
    return env->NewStringUTF(
            OBFUSCATE("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"));
}

jstring IconWebViewData(JNIEnv *env, jobject thiz) {

    //WebView support GIF animation. Upload your image or GIF on imgur.com or other sites

    // From internet (Requires android.permission.INTERNET)
    // return env->NewStringUTF(OBFUSCATE("https://i.imgur.com/SujJ85j.gif"));

    // Base64 html:
    // return env->NewStringUTF("data:image/png;base64, <encoded base64 here>");

    // To disable it, return nullptr. It will use normal image above:
    // return nullptr

    //return env->NewStringUTF(OBFUSCATE_KEY("https://i.imgur.com/SujJ85j.gif", 'u'));
    return nullptr;
}

jobjectArray SettingsList(JNIEnv *env, jobject activityObject) {
    jobjectArray ret;

    const char *features[] = {
            OBFUSCATE("Category_Settings"),
            OBFUSCATE("-1_Toggle_Save feature preferences"), //-1 is checked on Preferences.java
            OBFUSCATE("-3_Toggle_Auto size vertically"),
            OBFUSCATE("Category_Menu"),
            OBFUSCATE("-6_Button_Back to Menu"),
    };

    int Total_Feature = (sizeof features /
                         sizeof features[0]); //Now you dont have to manually update the number everytime;
    ret = (jobjectArray)
            env->NewObjectArray(Total_Feature, env->FindClass(OBFUSCATE("java/lang/String")),
                                env->NewStringUTF(""));
    int i;
    for (i = 0; i < Total_Feature; i++)
        env->SetObjectArrayElement(ret, i, env->NewStringUTF(features[i]));

    return (ret);
}
