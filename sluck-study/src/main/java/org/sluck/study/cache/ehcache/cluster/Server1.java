package org.sluck.study.cache.ehcache.cluster;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import java.math.BigDecimal;

/**
 * Created by sunxy on 2016/4/21 20:37.
 */
public class Server1 {
    public static void main(String[] args) throws InterruptedException {

        CacheManager manager = CacheManager.newInstance("D:\\ideawork2\\finance\\finance-Interaction\\src\\test\\java\\com\\ecpss\\finance\\cache\\distribute\\cluster\\ehcache1.xml");
        manager.getTransactionController().setDefaultTransactionTimeout(50000);
        Cache cache = manager.getCache("demoTest");
        for (int i=0; i<200; i++) {
            Thread.currentThread().sleep(100);
            manager.getTransactionController().begin();
            People p1 = new People("sunxy" + i);
            cache.put(new Element(p1.getName(), p1));
            manager.getTransactionController().commit();
            System.out.println("add ok");
        }

        for (int i=199;i>=100;i--) {
            manager.getTransactionController().begin();
            Element e = cache.get("sunxy" + i);
            System.out.println(e == null);
            if (e != null) {
                System.out.println(e.getObjectValue().toString());
            }
            cache.remove("sunxy" + i);
            manager.getTransactionController().commit();
            System.out.println("remove ok");
        }

    }
}
