import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/7/29
 */
public class ThreadPrint {
    static Lock lock = new ReentrantLock(true);
    static final int PRINT_COUNT = 10;
    static final  int THREAD_COUNT = 3;
    static volatile  int count = 0;

    static Condition A = lock.newCondition();
    static Condition B = lock.newCondition();
    static Condition C = lock.newCondition();

    static class  PrintA implements Runnable{
        @Override
        public void run() {
            for (int i =  0; i< PRINT_COUNT;){
                lock.lock();
                try {
                    while (count% THREAD_COUNT != 0){
                        A.await();
                    }
                    System.out.println("A");
                    count++;
                    i++;
                    B.signal();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    lock.unlock();
                }
            }
        }
    }
    static class  PrintB implements Runnable{

        @Override
        public void run() {
            for (int i =  0; i< PRINT_COUNT;){
                lock.lock();
                try {
                    while (count% THREAD_COUNT != 1){
                        B.await();
                    }
                    System.out.println("B");
                    count++;
                    i++;
                    C.signal();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    lock.unlock();
                }
            }
        }
    }
    static class  PrintC implements Runnable{
        @Override
        public void run() {
            for (int i =  0; i< PRINT_COUNT;){
                lock.lock();
                try {
                    while (count% THREAD_COUNT != 2){
                        C.await();
                    }
                    System.out.println("C");
                    count++;
                    i++;
                    A.signal();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    lock.unlock();
                }
            }
        }
    }

    public static void main(String[] args) {
        ExecutorService service = Executors.newCachedThreadPool();
        service.execute(new PrintA());
        service.execute(new PrintB());
        service.execute(new PrintC());

    }
}
