//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.liferay.portal.servlet;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.MethodHandler;
import com.liferay.portal.kernel.util.MethodInvoker;
import com.liferay.portal.kernel.util.MethodWrapper;
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.HttpPrincipal;
import com.liferay.portal.security.auth.PrincipalThreadLocal;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.security.permission.PermissionThreadLocal;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalInstances;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TunnelServlet extends HttpServlet {
    private static Log _log = LogFactoryUtil.getLog(TunnelServlet.class);

    public TunnelServlet() {
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(request.getInputStream());
        } catch (IOException var17) {
            if (_log.isWarnEnabled()) {
                _log.warn(var17, var17);
            }

            return;
        }

        Object returnObj = null;

        try {
            ObjectValuePair<HttpPrincipal, Object> ovp = (ObjectValuePair)ois.readObject();
            HttpPrincipal httpPrincipal = (HttpPrincipal)ovp.getKey();
            Object ovpValue = ovp.getValue();
            MethodHandler methodHandler = null;
            MethodWrapper methodWrapper = null;
            if (ovpValue instanceof MethodHandler) {
                methodHandler = (MethodHandler)ovpValue;
            } else {
                methodWrapper = (MethodWrapper)ovpValue;
            }

            if (methodHandler != null) {
                if (!this.isValidRequest(methodHandler.getClassName())) {
                    return;
                }
            } else if (!this.isValidRequest(methodWrapper.getClassName())) {
                return;
            }

            long companyId = PortalInstances.getCompanyId(request);
            if (Validator.isNotNull(httpPrincipal.getLogin())) {
                User user = null;

                try {
                    user = UserLocalServiceUtil.getUserByEmailAddress(companyId, httpPrincipal.getLogin());
                } catch (NoSuchUserException var16) {
                }

                if (user == null) {
                    try {
                        user = UserLocalServiceUtil.getUserByScreenName(companyId, httpPrincipal.getLogin());
                    } catch (NoSuchUserException var15) {
                    }
                }

                if (user == null) {
                    try {
                        user = UserLocalServiceUtil.getUserById(GetterUtil.getLong(httpPrincipal.getLogin()));
                    } catch (NoSuchUserException var14) {
                    }
                }

                if (user != null) {
                    PrincipalThreadLocal.setName(user.getUserId());
                    PermissionChecker permissionChecker = PermissionCheckerFactoryUtil.create(user, true);
                    PermissionThreadLocal.setPermissionChecker(permissionChecker);
                }
            }

            if (methodHandler != null) {
                returnObj = methodHandler.invoke(true);
            } else {
                returnObj = MethodInvoker.invoke(methodWrapper);
            }
        } catch (InvocationTargetException var18) {
            returnObj = var18.getCause();
            if (!(returnObj instanceof PortalException)) {
                var18.printStackTrace();
                returnObj = new SystemException();
            }
        } catch (Exception var19) {
            _log.error(var19, var19);
        }

        if (returnObj != null) {
            ObjectOutputStream oos = new ObjectOutputStream(response.getOutputStream());
            oos.writeObject(returnObj);
            oos.flush();
            oos.close();
        }

    }

    protected boolean isValidRequest(String className) {
        return className.contains(".service.") && className.endsWith("ServiceUtil") && !className.endsWith("LocalServiceUtil");
    }
}
