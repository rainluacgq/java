package thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/7/2
 */
public class RetrantLockTest {
     static  ReentrantLock lock = new ReentrantLock();
    static  volatile int count = 0;

    static  final  int MAX_COUNT = 10;
    static  final  int MIN_COUNT = 0;

    static  Condition full = lock.newCondition();
    static Condition empty = lock.newCondition();


    static class  Consumer implements  Runnable{
        @Override
        public void run() {
            for (int i = 0; i < MAX_COUNT; i++) {
                lock.lock();
                try {
                    while (count == MIN_COUNT) {
                        empty.await();
                    }
                    count--;
                    full.signalAll();
                    System.out.println(Thread.currentThread().getId() + "消费者" + count);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }
    }


    static class  Producer implements  Runnable{

        @Override
        public void run() {
            for (int i = 0; i < MAX_COUNT; i++) {
                lock.lock();
                try {
                    while (count == MAX_COUNT) {
                        full.await();
                    }
                    count++;
                    System.out.println(Thread.currentThread().getId() + "生成者" + count);
                    empty.signalAll();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }
    }


    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0;i< MAX_COUNT;i++){
            executorService.execute( new Producer());
            executorService.execute(new Consumer());
        }
        executorService.shutdown();
    }
}
