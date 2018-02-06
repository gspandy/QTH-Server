//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.nowbook.site.handlebars;

import com.nowbook.common.utils.JsonMapper;
import com.nowbook.site.dao.ComponentDao;
import com.nowbook.site.handlebars.HandlebarEngine;
import com.nowbook.site.model.FullPage;
import com.github.jknack.handlebars.Handlebars.SafeString;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.html.HtmlEscapers;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Component
public class RenderHelpers {
    private static final Logger log = LoggerFactory.getLogger(RenderHelpers.class);
    @Autowired
    private HandlebarEngine handlebarEngine;
    @Autowired
    private ComponentDao componentDao;

    public RenderHelpers() {
    }

    @PostConstruct
    public void init() {
        this.handlebarEngine.registerHelper("render", new Helper<String>() {


            public CharSequence apply(String type, Options options) throws IOException {
                FullPage fullpage = (FullPage)options.get("_PAGE_");
                String templateStr = null;
                if(type.equals("header")) {
                    templateStr = fullpage.getHeader();
                } else if(type.equals("footer")) {
                    templateStr = fullpage.getFooter();
                } else if(type.equals("body")) {
                    templateStr = fullpage.getFixed();
                } else if(type.equals("part")) {
                    Object context = options.param(0);
                    if(context == null) {
                        RenderHelpers.log.error("part key not found when rendering fullPage: ({})", fullpage);
                    } else if(fullpage.getParts() != null) {
                        templateStr = (String)fullpage.getParts().get(String.valueOf(context));
                    } else {
                        RenderHelpers.log.warn("part ({}) not found for fullPage: ({})", context, fullpage);
                    }
                }

                if(Strings.isNullOrEmpty(templateStr)) {
                    RenderHelpers.log.warn("can\'t find template when render block: {}, instanceId: {}, pageId: {}, pagePath: {}", new Object[]{type, fullpage.getInstanceId(), fullpage.getId(), fullpage.getPath()});
                    return (CharSequence)(options.tagType == TagType.SECTION?options.fn():"");
                } else {
                    Map context1 = null;
                    if(options.context.model() instanceof Map) {
                        context1 = (Map)options.context.model();
                    }

                    return new SafeString(RenderHelpers.this.handlebarEngine.execInline(templateStr, context1));
                }
            }
        });
        this.handlebarEngine.registerHelper("inject", new Helper<String>() {
            public CharSequence apply(String compPath, Options options) throws IOException {
                boolean isDesignMode = options.get("_DESIGN_MODE_") != null;
                HashMap tempContext = Maps.newHashMap();
                if(options.context.model() instanceof Map) {
                    tempContext.putAll((Map)options.context.model());
                    Set gdataJson = (Set)tempContext.remove("_CDATA_KEYS_");
                    if(gdataJson != null) {
                        Iterator component = gdataJson.iterator();

                        while(component.hasNext()) {
                            String firstParam = (String)component.next();
                            tempContext.remove(firstParam);
                        }
                    }

                    Set component2 = (Set)tempContext.remove("_GDATA_KEYS_");
                    if(component2 != null) {
                        Iterator firstParam1 = component2.iterator();

                        while(firstParam1.hasNext()) {
                            String paths = (String)firstParam1.next();
                            tempContext.remove(paths);
                        }
                    }

                    if(isDesignMode) {
                        tempContext.remove("_COMP_DATA_");
                        tempContext.remove("_COMP_GDATA_");
                    }
                }

                String gdataJson1;
                if(tempContext.containsKey("_DESIGN_GDATA_")) {
                    gdataJson1 = (String)tempContext.remove("_DESIGN_GDATA_");
                } else {
                    gdataJson1 = RenderHelpers.this.componentDao.getData(compPath);
                }

                Map component1;
                if(!Strings.isNullOrEmpty(gdataJson1)) {
                    component1 = (Map)JsonMapper.nonEmptyMapper().fromJson(gdataJson1, Map.class);
                    if(component1 != null && !component1.isEmpty()) {
                        tempContext.put("_GDATA_KEYS_", component1.keySet());
                        tempContext.putAll(component1);
                        if(isDesignMode) {
                            tempContext.put("_COMP_GDATA_", HtmlEscapers.htmlEscaper().escape(gdataJson1));
                        }
                    }
                }

                if(options.tagType == TagType.SECTION && StringUtils.isNotBlank(options.fn.text())) {
                    component1 = (Map)JsonMapper.nonEmptyMapper().fromJson(options.fn.text(), Map.class);
                    if(component1 != null && !component1.isEmpty()) {
                        tempContext.put("_CDATA_KEYS_", component1.keySet());
                        tempContext.putAll(component1);
                        if(isDesignMode) {
                            tempContext.put("_COMP_DATA_", HtmlEscapers.htmlEscaper().escape(options.fn.text().trim()));
                        }
                    }
                }

                tempContext.put("_COMP_PATH_", compPath);
                com.nowbook.site.model.Component component3 = new com.nowbook.site.model.Component();
                component3.setPath(compPath);
                Object firstParam2 = options.param(0, (Object)null);
                if(firstParam2 != null && firstParam2 instanceof String && StringUtils.isNotBlank((String)firstParam2)) {
                    component3.setApi((String)firstParam2);
                } else {
                    com.nowbook.site.model.Component paths2 = RenderHelpers.this.componentDao.findByPath(compPath);
                    if(paths2 != null) {
                        component3 = paths2;
                    } else {
                        RenderHelpers.log.warn("can\'t find component config for path:{}", compPath);
                    }
                }

                if(isDesignMode) {
                    String[] paths1 = compPath.split("/");
                    tempContext.put("_COMP_NAME_", Objects.firstNonNull(component3.getName(), paths1[paths1.length - 1]));
                }

                return new SafeString(RenderHelpers.this.handlebarEngine.execComponent(component3, tempContext));
            }
        });
        this.handlebarEngine.registerHelper("component", new Helper<String>() {
            public CharSequence apply(String className, Options options) throws IOException {
                boolean isDesignMode = options.get("_DESIGN_MODE_") != null;
                className = className + " rob-component";
                Object customClassName = options.context.get("_CLASS_");
                StringBuilder compOpenTag = (new StringBuilder("<div class=\"")).append(className);
                if(customClassName != null) {
                    compOpenTag.append(" ").append(customClassName);
                }

                compOpenTag.append("\"");
                Object style = options.context.get("_STYLE_");
                if(style != null) {
                    compOpenTag.append(" style=\"").append(style).append("\"");
                }

                Object compName = options.context.get("_COMP_NAME_");
                if(compName != null) {
                    compOpenTag.append(" data-comp-name=\"").append(compName).append("\"");
                }

                Object compData = options.context.get("_COMP_DATA_");
                if(compData != null) {
                    compOpenTag.append(" data-comp-data=\"").append(compData).append("\"");
                }

                Object compGData = options.context.get("_COMP_GDATA_");
                if(compGData != null) {
                    compOpenTag.append(" data-comp-g-data=\"").append(compGData).append("\"");
                }

                Object compPath = options.context.get("_COMP_PATH_");
                if(compPath != null) {
                    compOpenTag.append(" data-comp-path=\"").append(compPath).append("\"");
                }

                if(isDesignMode) {
                    compOpenTag.append(" data-comp-class=\"").append(className).append("\"");
                }

                compOpenTag.append(" >");
                return new SafeString(compOpenTag.toString() + options.fn() + "</div>");
            }
        });
    }
}
