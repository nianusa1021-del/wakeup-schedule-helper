package cn.dorimu.lsp.wakeuphelper;

import android.app.Activity;
import android.content.Context;
import android.util.Pair;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class WakeupAdsHook implements IXposedHookLoadPackage {
    private static final String TARGET_PACKAGE = "com.suda.yzune.wakeupschedule";
    private static final int BLOCK_CODE = -10086;
    private static final String BLOCK_MSG = "blocked by wakeup-lsp";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!TARGET_PACKAGE.equals(lpparam.packageName)) {
            return;
        }
        log("start hook for " + lpparam.packageName);
        hookAppLevel(lpparam.classLoader);
        hookFastAdCore(lpparam.classLoader);
        hookThirdPartySdk(lpparam.classLoader);
        hookBottomNavigation(lpparam.classLoader);
    }

    private static void hookAppLevel(ClassLoader cl) {
        Class<?> lifecycleClass = findClass("androidx.lifecycle.Lifecycle", cl);
        Class<?> splashCallbackClass = findClass("o00oOo0o.o0Oo0oo", cl);
        if (lifecycleClass != null && splashCallbackClass != null) {
            hookMethod("o00OoO00.OooO0OO", cl, "OooO00o",
                    Activity.class, lifecycleClass, splashCallbackClass,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            Object cb = param.args[2];
                            if (cb != null) {
                                callMethodSafe(cb, "OooO0OO", false);
                                callMethodSafe(cb, "OooO0O0");
                            }
                            param.setResult(null);
                        }
                    });
        }

        // 广告 SDK 初始化任务
        hookAllMethods("o00OooO0.o0000O0", cl, "run", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });

        // 开屏广告动态配置任务
        hookAllMethods("o00Ooo0O.OooOo00", cl, "run", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });

        // /wakeup/app/conf 拉取开屏广告配置
        hookAllMethods("com.suda.yzune.wakeupschedule.aaa.utils.OooOOOO", cl, "OooO0OO", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });

        // 首页弹窗广告位（/wakeup/app/bannerconf）
        Class<?> componentActivityClass = findClass("androidx.activity.ComponentActivity", cl);
        if (componentActivityClass != null) {
            hookMethod("com.suda.yzune.wakeupschedule.manage.HomeDialogHelper", cl, "OooO",
                    componentActivityClass, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.setResult(null);
                        }
                    });
        }

        // Mine 页轮播广告位
        hookMethod("com.suda.yzune.wakeupschedule.aaa.fragment.MineBannerAdapter", cl, "OooOOo",
                List.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.args[0] = Collections.emptyList();
                    }
                });

        // 课表页顶部运营位
        final Object kotlinUnit = getKotlinUnit(cl);
        hookAllMethods("com.suda.yzune.wakeupschedule.schedule.ScheduleFragment", cl, "o0o0Oo", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(kotlinUnit);
            }
        });

        // 应用侧开屏管理器直接断开
        hookAllMethods("o00OoO0.OooO0OO", cl, "OooOO0O", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });
        hookAllMethodsCandidates(new String[]{
                        "com.suda.yzune.wakeupschedule.aaa.p945ad.splash.FastAdSplashManager",
                        "com.suda.yzune.wakeupschedule.aaa.ad.splash.FastAdSplashManager"
                },
                cl, "OooO00o", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.setResult(null);
                    }
                });
    }

    private static void hookFastAdCore(ClassLoader cl) {
        // 聚合 SDK 总初始化
        hookAllMethods("com.homework.fastad.FastAdSDK", cl, "OooOOo", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });

        // 广告策略/瀑布流动态配置
        hookAllMethods("com.homework.fastad.strategy.FastAdStrategyConfig", cl, "OooOOOO", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });
        hookAllMethods("com.homework.fastad.strategy.FastAdStrategyConfig", cl, "OooOOO", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args != null && param.args.length > 0 && param.args[0] != null) {
                    callMethodSafe(param.args[0], "onResponse", (Object) null);
                }
                param.setResult(null);
            }
        });

        // 请求广告素材总入口
        hookMethod("com.fastad.api.request.RequestApiAdManager", cl, "requestApiAd",
                int.class,
                findClass("com.homework.fastad.common.AdSlot", cl),
                findClass("com.fastad.api.request.RequestApiAdCallback", cl),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object cb = param.args[2];
                        if (cb != null) {
                            callMethodSafe(cb, "requestError", BLOCK_CODE, BLOCK_MSG);
                        }
                        param.setResult(null);
                    }
                });

        // 统一断开 FastAd SDK 的 codePos 请求
        hookMethod("com.homework.fastad.core.OooO0OO", cl, "Oooo00o",
                String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.setResult(null);
                    }
                });

        // 开屏展示
        hookMethod("com.fastad.api.splash.SplashAd", cl, "showAdView",
                Activity.class, findClass("com.fastad.api.splash.SplashAdActionListener", cl),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object listener = param.args[1];
                        if (listener != null) {
                            callMethodSafe(listener, "onRenderFail", BLOCK_CODE, BLOCK_MSG);
                            callMethodSafe(listener, "onSkippedAd");
                        }
                        param.setResult(null);
                    }
                });

        // Banner 展示
        hookMethod("com.fastad.api.banner.BannerAd", cl, "showAdView",
                Activity.class, ViewGroup.class, findClass("com.fastad.api.banner.BannerAdActionListener", cl),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object listener = param.args[2];
                        if (listener != null) {
                            callMethodSafe(listener, "onAdRenderFail", BLOCK_CODE, BLOCK_MSG);
                            callMethodSafe(listener, "onAdClose");
                        }
                        param.setResult(null);
                    }
                });

        // 信息流展示
        hookMethod("com.fastad.api.express.FlowExpressAd", cl, "showAdView",
                Activity.class, findClass("com.fastad.api.express.FlowExpressAdActionListener", cl),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object listener = param.args[1];
                        if (listener != null) {
                            callMethodSafe(listener, "onAdRenderFail", BLOCK_CODE, BLOCK_MSG);
                            callMethodSafe(listener, "onAdClose");
                        }
                        param.setResult(null);
                    }
                });

        // 插屏展示
        hookMethod("com.fastad.api.interstitial.InterstitialAd", cl, "showAd",
                Activity.class, findClass("com.fastad.api.interstitial.InterstitialAdActionListener", cl),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object listener = param.args[1];
                        if (listener != null) {
                            callMethodSafe(listener, "onAdRenderFail", BLOCK_CODE, BLOCK_MSG);
                            callMethodSafe(listener, "onAdClosed");
                        }
                        param.setResult(null);
                    }
                });

        // 激励视频展示
        hookMethod("com.fastad.api.reward.RewardVideoAd", cl, "showAd",
                Activity.class, findClass("com.fastad.api.reward.RewardAdActionListener", cl),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object listener = param.args[1];
                        if (listener != null) {
                            callMethodSafe(listener, "onShowError", BLOCK_CODE, BLOCK_MSG);
                            callMethodSafe(listener, "onAdClose");
                        }
                        param.setResult(null);
                    }
                });

        // 各家 ADN 初始化入口（聚合层）
        hookAdnInitWithFail(cl, "com.fastad.csj.FastAdCsjManager", "initCsjSDK");
        hookAdnInitWithFail(cl, "com.fastad.ylh.FastAdYlhManager", "initYlhSDK");
        hookAdnInitWithFail(cl, "com.fastad.baidu.FastAdBDManager", "initBaiduSDK");
        hookAdnInitWithFail(cl, "com.fastad.p609ks.FastAdKsManager", "initKsSdk");
    }

    private static void hookThirdPartySdk(ClassLoader cl) {
        // 穿山甲
        hookMethod("com.bytedance.sdk.openadsdk.TTAdSdk", cl, "init",
                Context.class, findClass("com.bytedance.sdk.openadsdk.TTAdConfig", cl),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.setResult(false);
                    }
                });
        hookAllMethods("com.bytedance.sdk.openadsdk.TTAdSdk", cl, "start", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });

        // 广点通
        hookMethod("com.p900qq.p901e.comm.managers.GDTAdSdk", cl, "init",
                Context.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.setResult(null);
                    }
                });
        hookMethod("com.p900qq.p901e.comm.managers.GDTAdSdk", cl, "initWithoutStart",
                Context.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.setResult(null);
                    }
                });
        hookAllMethods("com.p900qq.p901e.comm.managers.GDTAdSdk", cl, "start", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });

        // 广点通动态插件装载/更新
        hookAllMethods("com.p900qq.p901e.comm.managers.plugin.C9341PM", cl, "tryLockUpdate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(false);
            }
        });
        hookAllMethods("com.p900qq.p901e.comm.managers.plugin.C9341PM", cl, "getPOFactory", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });

        // 百度联盟
        hookAllMethods("com.baidu.mobads.sdk.api.BDAdConfig", cl, "preInit", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });
        hookAllMethods("com.baidu.mobads.sdk.api.BDAdConfig", cl, "init", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });

        // 快手
        hookMethod("com.kwad.sdk.api.KsAdSDK", cl, "init",
                Context.class, findClass("com.kwad.sdk.api.SdkConfig", cl),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.setResult(false);
                    }
                });
        hookAllMethods("com.kwad.sdk.api.KsAdSDK", cl, "start", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });

        // 百度 techain 动态调用链（可能下发/调度动态组件）
        hookAllMethods("com.baidu.techain.p091ac.C2333TH", cl, "init", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });
        hookAllMethods("com.baidu.techain.p091ac.C2333TH", cl, "initDelay", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });
        hookAllMethods("com.baidu.techain.p091ac.C2333TH", cl, "startPushService", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(null);
            }
        });
        hookAllMethods("com.baidu.techain.p091ac.C2333TH", cl, "tinvoke", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(false);
            }
        });
        hookAllMethods("com.baidu.techain.p091ac.C2333TH", cl, "tinvokeSync", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                param.setResult(new Pair<>(-1, null));
            }
        });
    }

    private static void hookAdnInitWithFail(ClassLoader cl, String className, String methodName) {
        Class<?> callbackClass = findClass("com.homework.fastad.util.OooO00o", cl);
        if (callbackClass == null) {
            return;
        }
        hookMethod(className, cl, methodName, callbackClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args != null && param.args.length > 0 && param.args[0] != null) {
                    callMethodSafe(param.args[0], "fail", BLOCK_CODE, BLOCK_MSG);
                }
                param.setResult(null);
            }
        });
    }

    private static Object getKotlinUnit(ClassLoader cl) {
        try {
            Class<?> unitClass = XposedHelpers.findClass("kotlin.o0OOO0o", cl);
            return XposedHelpers.getStaticObjectField(unitClass, "f56666OooO00o");
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void hookMethod(String className, ClassLoader cl, String methodName, Object... parameterTypesAndCallback) {
        try {
            XposedHelpers.findAndHookMethod(className, cl, methodName, parameterTypesAndCallback);
            log("hooked " + className + "#" + methodName);
        } catch (Throwable ignored) {
        }
    }

    private static void hookAllMethods(String className, ClassLoader cl, String methodName, XC_MethodHook callback) {
        try {
            Class<?> cls = XposedHelpers.findClass(className, cl);
            XposedBridge.hookAllMethods(cls, methodName, callback);
            log("hookedAll " + className + "#" + methodName);
        } catch (Throwable ignored) {
        }
    }

    private static void hookAllMethodsCandidates(String[] classNames, ClassLoader cl, String methodName, XC_MethodHook callback) {
        for (String className : classNames) {
            hookAllMethods(className, cl, methodName, callback);
        }
    }

    private static Class<?> findClass(String className, ClassLoader cl) {
        try {
            return XposedHelpers.findClass(className, cl);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void callMethodSafe(Object receiver, String method, Object... args) {
        try {
            XposedHelpers.callMethod(receiver, method, args);
        } catch (Throwable ignored) {
        }
    }

    private static void log(String msg) {
        XposedBridge.log("[WakeupLsp] " + msg);
    }

    private static void hookBottomNavigation(ClassLoader cl) {
        // Hook 主 Activity 的 onCreate 方法来隐藏底部导航栏
        try {
            Class<?> mainActivityClass = findClass("com.suda.yzune.wakeupschedule.MainActivity", cl);
            if (mainActivityClass == null) {
                // 尝试查找其他可能的主 Activity 类名
                mainActivityClass = findClass("com.suda.yzune.wakeupschedule.ui.MainActivity", cl);
            }
            
            if (mainActivityClass != null) {
                hookMethod(mainActivityClass.getName(), cl, "onCreate", 
                    android.os.Bundle.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            try {
                                Activity activity = (Activity) param.thisObject;
                                hideBottomNavigationView(activity);
                                adjustLayoutForFullScreen(activity);
                                log("Bottom navigation hidden successfully");
                            } catch (Throwable t) {
                                log("Error hiding bottom navigation: " + t.getMessage());
                            }
                        }
                    });
                log("Hooked MainActivity for bottom navigation removal");
            } else {
                log("MainActivity class not found, trying alternative approach");
                
                // 备用方案：尝试 Hook Navigation 相关的 Fragment
                hookNavigationFragment(cl);
            }
        } catch (Throwable t) {
            log("Failed to hook bottom navigation: " + t.getMessage());
        }
    }

    private static void hookNavigationFragment(ClassLoader cl) {
        // 尝试 Hook Navigation Fragment 容器来移除底部导航栏
        Class<?> navHostFragmentClass = findClass("androidx.navigation.fragment.NavHostFragment", cl);
        if (navHostFragmentClass != null) {
            hookAllMethods(navHostFragmentClass.getName(), cl, "onCreateView", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        android.view.View view = (android.view.View) param.getResult();
                        if (view != null) {
                            hideBottomNavigationInView(view);
                        }
                    } catch (Throwable t) {
                        log("Error in navigation fragment hook: " + t.getMessage());
                    }
                }
            });
            log("Hooked NavHostFragment for bottom navigation removal");
        }
    }

    private static void hideBottomNavigationView(Activity activity) {
        // 查找并隐藏 BottomNavigationView
        android.view.View rootView = activity.getWindow().getDecorView();
        hideBottomNavigationInView(rootView);
    }

    private static void hideBottomNavigationInView(android.view.View view) {
        // 递归查找 BottomNavigationView
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup viewGroup = (android.view.ViewGroup) view;
            
            // 首先尝试查找 BottomNavigationView
            android.view.View bottomNav = findViewByType(viewGroup, 
                "com.google.android.material.bottomnavigation.BottomNavigationView");
            
            if (bottomNav == null) {
                // 尝试其他可能的类名
                bottomNav = findViewByType(viewGroup, "androidx.appcompat.widget.BottomNavigationView");
            }
            
            if (bottomNav == null) {
                // 尝试通过资源ID查找
                bottomNav = findViewByResourceName(viewGroup, "bottom_navigation");
            }
            
            if (bottomNav != null) {
                // 隐藏底部导航栏
                bottomNav.setVisibility(android.view.View.GONE);
                
                // 尝试找到相邻的布局容器（可能是 FrameLayout 或 ConstraintLayout）
                // 并调整其布局参数以占据全屏
                adjustParentLayout(bottomNav);
                
                log("Found and hidden BottomNavigationView");
            } else {
                // 如果没有找到 BottomNavigationView，尝试查找包含底部导航的容器
                findAndHideBottomNavigationContainer(viewGroup);
            }
        }
    }

    private static android.view.View findViewByType(android.view.ViewGroup parent, String className) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            android.view.View child = parent.getChildAt(i);
            if (child.getClass().getName().equals(className)) {
                return child;
            }
            if (child instanceof android.view.ViewGroup) {
                android.view.View result = findViewByType((android.view.ViewGroup) child, className);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static android.view.View findViewByResourceName(android.view.ViewGroup parent, String resourceName) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            android.view.View child = parent.getChildAt(i);
            try {
                String resName = child.getResources().getResourceName(child.getId());
                if (resName.contains(resourceName)) {
                    return child;
                }
            } catch (Exception e) {
                // 忽略资源查找异常
            }
            if (child instanceof android.view.ViewGroup) {
                android.view.View result = findViewByResourceName((android.view.ViewGroup) child, resourceName);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static void findAndHideBottomNavigationContainer(android.view.ViewGroup parent) {
        // 查找可能包含底部导航的容器（通常是底部有固定高度的布局）
        for (int i = 0; i < parent.getChildCount(); i++) {
            android.view.View child = parent.getChildAt(i);
            if (child instanceof android.view.ViewGroup) {
                android.view.ViewGroup childGroup = (android.view.ViewGroup) child;
                
                // 检查布局参数，如果底部有固定高度，可能是导航容器
                android.view.ViewGroup.LayoutParams params = child.getLayoutParams();
                if (params instanceof android.widget.FrameLayout.LayoutParams) {
                    android.widget.FrameLayout.LayoutParams frameParams = 
                        (android.widget.FrameLayout.LayoutParams) params;
                    if (frameParams.gravity == (android.view.Gravity.BOTTOM | android.view.Gravity.START) ||
                        frameParams.gravity == android.view.Gravity.BOTTOM) {
                        child.setVisibility(android.view.View.GONE);
                        adjustParentLayout(child);
                        log("Found and hidden bottom navigation container by layout gravity");
                        return;
                    }
                } else if (params instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams constraintParams = 
                        (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) params;
                    if (constraintParams.bottomToBottom == androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID &&
                        constraintParams.topToTop == -1) {
                        child.setVisibility(android.view.View.GONE);
                        adjustParentLayout(child);
                        log("Found and hidden bottom navigation container by constraint layout");
                        return;
                    }
                }
                
                // 递归查找
                findAndHideBottomNavigationContainer(childGroup);
            }
        }
    }

    private static void adjustParentLayout(android.view.View view) {
        // 调整父布局参数，使内容区域扩展
        android.view.View parent = (android.view.View) view.getParent();
        if (parent instanceof android.view.ViewGroup) {
            android.view.ViewGroup parentGroup = (android.view.ViewGroup) parent;
            
            // 查找可能是内容区域的兄弟视图
            for (int i = 0; i < parentGroup.getChildCount(); i++) {
                android.view.View sibling = parentGroup.getChildAt(i);
                if (sibling != view) {
                    // 调整兄弟视图的布局参数以占据全屏
                    adjustSiblingLayout(sibling);
                }
            }
        }
    }

    private static void adjustSiblingLayout(android.view.View view) {
        // 调整视图的布局参数以占据全屏
        android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
        
        if (params instanceof android.widget.FrameLayout.LayoutParams) {
            android.widget.FrameLayout.LayoutParams frameParams = 
                (android.widget.FrameLayout.LayoutParams) params;
            // 移除底部约束，让内容占据全屏
            frameParams.gravity = android.view.Gravity.FILL;
            view.setLayoutParams(frameParams);
        } else if (params instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams constraintParams = 
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) params;
            // 设置到底部约束为父布局底部
            constraintParams.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            view.setLayoutParams(constraintParams);
        } else if (params instanceof android.widget.RelativeLayout.LayoutParams) {
            android.widget.RelativeLayout.LayoutParams relativeParams = 
                (android.widget.RelativeLayout.LayoutParams) params;
            // 添加到底部对齐
            relativeParams.addRule(android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM);
            view.setLayoutParams(relativeParams);
        }
    }

    private static void adjustLayoutForFullScreen(Activity activity) {
        // 确保内容区域占据全屏的备用方法
        android.view.View contentView = activity.findViewById(android.R.id.content);
        if (contentView != null) {
            android.view.ViewGroup contentParent = (android.view.ViewGroup) contentView.getParent();
            if (contentParent != null) {
                // 设置内容区域填充整个屏幕
                contentParent.setPadding(0, 0, 0, 0);
            }
        }
    }
}
