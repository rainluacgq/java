package thread;

import com.sun.jmx.snmp.tasks.ThreadService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/7/4
 */
public class PrintThread {
    static volatile  int count = 1;
    static volatile int status = 0;
    static final  int MAX_COUNT = 100;
    static  final  int THREAD_COUNT = 3;
    static Object lock = new Object();


    static class  Thread1 implements  Runnable{
        int flag = 0;   //标志
        Thread1(int n){
            this.flag = n;
        }

        @Override
        public void run() {
            while (count < MAX_COUNT) {
                if(count > MAX_COUNT) {
                    return;
                }
                synchronized (lock) {
                    while (status % THREAD_COUNT != this.flag) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    for (int i = count; i < count + THREAD_COUNT; i++) {
                        if(i > MAX_COUNT) {
                            break;
                        }
                        System.out.println(i + "thread id" + Thread.currentThread().getId());
                    }
                    count += 3;
                    status++;
                    lock.notifyAll();
                }
            }
        }
    }

    public static void main(String[] args) {
        ExecutorService service = Executors.newCachedThreadPool();
        for(int i = 0;i< THREAD_COUNT;i++){
            service.execute(new Thread1(i));
        }
        service.shutdown();

    }

}
