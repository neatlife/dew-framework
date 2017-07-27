package com.tairanchina.csp.dew.core.jdbc.proxy;

import com.ecfront.dew.common.$;
import com.ecfront.dew.common.Page;
import com.tairanchina.csp.dew.core.entity.EntityContainer;
import com.tairanchina.csp.dew.core.jdbc.annotations.ModelParam;
import com.tairanchina.csp.dew.core.jdbc.annotations.Param;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 迹_Jason on 2017/7/24.
 * 方法相关信息解析
 */
public class MethodConstruction {

    private Method method;

    private Object[] params;

    private Annotation[] methodAnnotations;

    private Annotation[][] paramAnnotations;

    private Map<String, Object> paramsMap;

    public MethodConstruction(Method method, Object[] args) {
        setMethod(method);
        setMethodAnnotations(method.getAnnotations());
        setParamAnnotations(method.getParameterAnnotations());
        setParams(args);
        bindingParams(method.getParameters(), args);
    }

    private void bindingParams(Parameter[] paramNames, Object[] paramValues) {
        paramsMap = new HashMap<>();
        int i = 0;
        for (Object v : paramValues) {
            if (v != null && v.toString().length() > 0) {
                if (containAnnotation(paramAnnotations[i])) {
                    transformToMap(v, paramsMap);
                } else {
                    Annotation paramAnn = getParamOfName(paramNames[i].getAnnotations());
                    if (paramAnn != null) {
                        paramsMap.put(((Param) paramAnn).value(), v);
                    }
                }
            }
            i++;
        }
    }

    private Annotation getParamOfName(Annotation[] paramAnnotations) {
        for (Annotation ann : paramAnnotations) {
            if (ann instanceof Param)
                return ann;
        }
        return null;
    }

    private boolean containAnnotation(Annotation[] paramAnnotations) {
        for (Annotation ann : paramAnnotations) {
            if (ann instanceof ModelParam)
                return true;
        }
        return false;
    }

    public void transformToMap(Object entities, Map<String, Object> paramsMap) {
        EntityContainer.EntityClassInfo entityClassInfo = EntityContainer.getCodeFieldNameByClazz(entities.getClass());
        Map<String, Object> values = $.bean.findValues(entities, null, null, entityClassInfo.columns.keySet(), null);
        values.forEach((k, v) -> {
            if (v != null && v.toString().length() > 0) {
                paramsMap.put(k, v);
            }
        });
    }

    public Object[] getParameters() {
        return method.getParameters();
    }

    public Annotation[] getMethodAnnotions() {
        return methodAnnotations;
    }

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    public boolean flagOfPaging() {
        return method.getReturnType().isAssignableFrom(Page.class);
    }

    public long getPageNumber() {
        if (flagOfPaging()) {
            return (long) paramsMap.get("pageNumber");
        }
        return 1;
    }

    public int getPageSize() {
        if (flagOfPaging()) {
            return (int) paramsMap.get("pageSize");
        }
        return 10;
    }

    private void setMethod(Method method) {
        this.method = method;
    }

    private void setMethodAnnotations(Annotation[] methodAnnotations) {
        this.methodAnnotations = methodAnnotations;
    }

    private void setParamAnnotations(Annotation[][] paramAnnotations) {
        this.paramAnnotations = paramAnnotations;
    }

    private void setParams(Object[] params) {
        this.params = params;
    }

    public Object[] getParams() {
        return params;
    }

    public Map<String, Object> getParamsMap() {
        return paramsMap;
    }
}
