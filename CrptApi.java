import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CrptApi {

    private final int requestLimit;
    private final long timeLimit;
    private final AtomicInteger requestCount = new AtomicInteger();
    private static final Lock lock = new ReentrantLock();
    private long lastTime;

    private final Logger logger = Logger.getLogger("CrptApiLog");

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        timeLimit = timeUnit.toMillis(1);
    }

    public static void main(String[] args) {
        CrptApi api = new CrptApi(TimeUnit.SECONDS, 100);
        while(true) {
            Object document = new Object();
            String sign = "sign";
            lock.lock();
            Document request = api.createDocument(document, sign);
            lock.unlock();
        }
    }

    public Document createDocument(Object document, String sign) {
        int currentCount = requestCount.incrementAndGet();
        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - lastTime;
        if (timePassed >= timeLimit) {
            requestCount.set(0);
            lastTime = currentTime;
        } else if (currentCount > requestLimit) {
            try {
                Thread.sleep(timeLimit - timePassed);
            } catch (InterruptedException e) {
                logger.log(Level.INFO, "Thread was interrupted");
            }
        }
        return new Document(document, sign);
    }

    public static class Document {
        private final Object document;
        private final String sign;

        public Document(Object document, String sign) {
            this.document = document;
            this.sign = sign;
        }
    }

}
