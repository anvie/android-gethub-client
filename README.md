

Gethub Chat Client for Android
================================


Gethub is lightweight scalable chat engine server.
This is Gethub client example for Android.

Tested and working using Gethub version 0.0.10.

Included Gethub Client Library
================================

Usage example:

```
GethubClient gethub = GethubClient.getInstance();

// set your gethub server address here.
gethub.connect("10.0.2.2", 6060);

String sessid = gethub.authorize(intent.getStringExtra("userName"), intent.getStringExtra("password"));

if(sessid == null){
    Log.i(TAG, "Cannot authorize user");
    return;
}

if(!gethub.join(CHANNEL, sessid)){
    Log.i(TAG, "Cannot join to channel");
    return;
}

gethub.bind(CHANNEL, sessid, new DataReceiver(gethub, sessid));
```

Data receiver is:

```
/**
 * Class ini digunakan sebagai data receiver
 * ketika binding sukses maka class ini
 * akan mendapatkan push data dari server.
 */
private class DataReceiver extends GethubClientDataReceiver {
    private GethubClient gethub = null;
    private String sessid = null;

    public DataReceiver(GethubClient gethub, String sessid){
        this.gethub = gethub;
        this.sessid = sessid;
    }

    /**
     * Implementasi method ini untuk menghandle
     * data dari server.
     * data sudah berupa {{JSONObject}}
     * @param data data dari server.
     */
    @Override
    public void onReceive(JSONObject data) {
        Log.d(TAG, "receiver got: " + data.toString());

        try {
            String ns = data.getString("ns");
            if (ns.equals(NS_MESSAGE)){
                String message = data.getString("message");
                Log.i(TAG, "from " + data.getString("from") + ": " + message);
                gethub.message(CHANNEL, message + "? apa itu?", sessid);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}
```
