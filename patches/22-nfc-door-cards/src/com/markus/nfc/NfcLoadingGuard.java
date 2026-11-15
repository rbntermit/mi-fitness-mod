package com.markus.nfc;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.xiaomi.fitness.baseui.view.BaseActivity;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;

/** Bounds the blocking overlay; Xiaomi's read-only load continues and may still update the screen. */
public final class NfcLoadingGuard {
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private static final WeakHashMap<BaseActivity,Owner> OWNERS=new WeakHashMap<>();
    private static final long TIMEOUT_MS=30000;
    private static final class Owner {
        final NfcLoadState state=new NfcLoadState();
        Callback current;
    }
    private static final class Callback implements Function3<Object,Object,Object,Unit> {
        final WeakReference<BaseActivity> screen;
        final Owner owner;
        final long generation;
        Function3 target;
        final Runnable deadline;
        Callback(BaseActivity a,Owner owner,long generation,Function3 target) {
            screen=new WeakReference<>(a);this.owner=owner;this.generation=generation;this.target=target;
            deadline=()->{
                BaseActivity current=screen.get();
                if(alive(current) && owner.state.timeout(generation)) {
                    current.hideLoading();
                    int id=current.getResources().getIdentifier("markus_nfc_list_timeout","string",current.getPackageName());
                    if(id!=0)Toast.makeText(current,current.getString(id),Toast.LENGTH_LONG).show();
                    Log.i("MOD","nfc: card list still loading; blocking overlay closed after 30s");
                }
            };
        }
        void release(){MAIN.removeCallbacks(deadline);target=null;}
        @Override public Unit invoke(Object first,Object second,Object third) {
            Runnable result=()->{
                BaseActivity a=screen.get();
                if(!alive(a) || !owner.state.complete(generation)){release();return;}
                Function3 callback=target;
                release();
                if(owner.current==this)owner.current=null;
                if(callback!=null)callback.invoke(first,second,third);
            };
            if(Looper.myLooper()==Looper.getMainLooper())result.run();else MAIN.post(result);
            return Unit.INSTANCE;
        }
    }
    private static boolean alive(BaseActivity a){return a!=null && !a.isFinishing() && !a.isDestroyed();}
    public static Function3 wrap(BaseActivity a,Function3 target) {
        Owner owner=OWNERS.get(a);
        if(owner==null){owner=new Owner();OWNERS.put(a,owner);}
        if(owner.current!=null)owner.current.release();
        Callback cb=new Callback(a,owner,owner.state.begin(),target);owner.current=cb;
        MAIN.postDelayed(cb.deadline,TIMEOUT_MS);
        return cb;
    }
    public static void close(BaseActivity a) {
        Owner owner=OWNERS.remove(a);
        if(owner!=null){owner.state.destroy();if(owner.current!=null){owner.current.release();owner.current=null;}}
    }
}
