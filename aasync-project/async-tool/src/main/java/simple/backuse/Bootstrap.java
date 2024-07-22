package simple.backuse;

import java.util.concurrent.TimeUnit;

public class Bootstrap {
    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        Worker worker = bootstrap.createWorker();

        Wrapper wrapper = new Wrapper();
        wrapper.setWorker(worker);
        wrapper.setParams("处理完了~~~~~~");

        bootstrap.doWork(wrapper)
                .addListener(result ->
                    System.out.println(Thread.currentThread().getName() + " : " + result.toString())
                );
        System.out.println(Thread.currentThread().getName());
    }
    private Worker createWorker() {
        return object -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "结果：" + object.toString();
        };
    }

    private Wrapper doWork(Wrapper wrapper) {
        new Thread(()->{
            Worker worker = wrapper.getWorker();
            String resultStr = worker.action(wrapper.getParams());
            wrapper.getListener().result(resultStr);
        }).start();
        return wrapper;
    }
}
