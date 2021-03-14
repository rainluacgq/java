import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/7/18
 */
class MyThread {
    static  final  int MAX_COUNT = 20;
    static Semaphore semaphore1 = new Semaphore(0);
    static Semaphore semaphore2 = new Semaphore(1);


    static class PrintBar implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i<  MAX_COUNT;) {
                try {
                    semaphore1.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("bar");
                i++;
                semaphore2.release();
            }
        }
    }

    static class  PrintFoo implements Runnable{
        @Override
        public void run() {
            for (int i = 0; i< MAX_COUNT;) {
                try {
                    semaphore2.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("foo");
                i++;
                semaphore1.release();
            }
        }
    }


    public static void main(String[] args) {
        ExecutorService service = Executors.newCachedThreadPool();

        long time =  System.currentTimeMillis();
        service.execute(new PrintFoo() );
        service.execute(new PrintBar());

     //   System.out.println(System.currentTimeMillis() - time);
        service.shutdown();

    }



}

