package com.palm1.jerb.platform;

import java.util.ServiceLoader;

public class Services {
    public static final IPlatformHelper PLATFORM = ServiceLoader.load(IPlatformHelper.class).findFirst().orElseThrow();
}
