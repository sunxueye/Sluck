package org.sluck.study.cache.ehcache.cluster;


import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * Created by sunxy on 2016/4/21 20:50.
 */
public class Server2 {

    public static void main(String[] args) throws InterruptedException {
        CacheManager manager = CacheManager.newInstance("D:\\ideawork2\\finance\\finance-Interaction\\src\\test\\java\\com\\ecpss\\finance\\cache\\distribute\\cluster\\ehcache2.xml");

        Thread.currentThread().sleep(20000);
        People p1 = new People("sunxy");
        Cache cache = manager.getCache("demoTest");
//        cache.put(new Element(p1.getName(), p1));
        manager.getTransactionController().begin();
        Element e = cache.get("sunxy0");
        System.out.println(e == null);
        if (e != null) {
            System.out.println(e.getObjectValue().toString());
        }
        manager.getTransactionController().commit();
        System.out.println("print ok");
       /* for (int i=0; i<200;i++) {
            Thread.currentThread().sleep(200);

        }*/

//        manager.getTransactionController().begin();
//        cache.remove("sunxy");
//        manager.getTransactionController().commit();


    }
}
