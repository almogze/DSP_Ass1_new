public class ThreadPerClient {

    public ThreadPerClient() {
    }

    protected void execute(Handler handler) {
            new Thread(handler).start();
        }
    }
