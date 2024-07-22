package simple.backuse;

public class Wrapper {
    private Object params;
    private Worker worker;
    private Listener listener;

    public Wrapper() {
    }

    public Wrapper(Object params, Worker worker, Listener listener) {
        this.params = params;
        this.worker = worker;
        this.listener = listener;
    }

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public Listener getListener() {
        return listener;
    }

    public void addListener(Listener listener) {
        this.listener = listener;
    }
}
