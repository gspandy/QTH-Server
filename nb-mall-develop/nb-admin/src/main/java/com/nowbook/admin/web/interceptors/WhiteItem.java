package com.nowbook.admin.web.interceptors;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by robin on 17/7/30.
 */
public  class WhiteItem {
    public final Pattern pattern;

    public final Set<String> httpMethods;

    public WhiteItem(Pattern pattern, Set<String> httpMethods) {
        this.pattern = pattern;
        this.httpMethods = httpMethods;
    }
}
