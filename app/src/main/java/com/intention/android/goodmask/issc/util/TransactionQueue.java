// vim: et sw=4 sts=4 tabstop=4
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.intention.android.goodmask.issc.util;

import java.util.ArrayDeque;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class TransactionQueue {

    private Consumer mConsumer;

    private Handler mHandler;

    private ArrayDeque<Transaction> mQueue;
    private Transaction mWorkingTransaction;

    private final static int TRANSACTION_DONE = 0x9527;
    private final static int REQUEST_CONSUME  = 0x9528;

    private boolean SpeedUpTransaction = true;
    //private boolean SpeedUpTransaction = false;

    public TransactionQueue(Consumer consumer) {
        HandlerThread thread = new HandlerThread("TransactionQueue");
        thread.start();
        mHandler = new QueueHandler(thread.getLooper());
        mQueue = new ArrayDeque<Transaction>();
        mConsumer = consumer;
    }

    public void add(Transaction t) {
        synchronized(this) {
        addTransaction(t);
        process();
        }
    }

    public void addFirst(Transaction t) {
        synchronized(this) {
            mQueue.addFirst(t);
        }
	}
    
    public int size() {
        synchronized(this) {
            return mQueue.size();
        }
    }

    public void clear() {
        synchronized(this) {
            mQueue.clear();
            mWorkingTransaction = null;
        }
    }

    public void process() {
        synchronized(this) {
        requestConsume();
        }
    }

    public void onConsumed() {
        synchronized(this)
        {
            if(SpeedUpTransaction) {
                //doneTransaction(1);
                doneTransaction(0);
            }
            else {
                doneTransaction(50);
            }
        }
    }

    public void destroy() {
        clear();
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mConsumer = null;
    }

    private void addTransaction(Transaction trans) {
        synchronized(this) {
            mQueue.add(trans);
        }
    }

    private void requestConsume() {
        synchronized(this) {
        mHandler.removeMessages(REQUEST_CONSUME);

        Message msg = mHandler.obtainMessage(REQUEST_CONSUME);
        msg.what = REQUEST_CONSUME;
        mHandler.sendMessage(msg);
        }
    }

    private void doneTransaction(long ms) {
        synchronized(this) {
        mHandler.removeMessages(TRANSACTION_DONE);
        Message msg = mHandler.obtainMessage(TRANSACTION_DONE);
        msg.what = TRANSACTION_DONE;

        Log.d("doneTransaction " + ms);

        if (ms > 0) {
            mHandler.sendMessageDelayed(msg, ms);
        } else {
            mHandler.sendMessage(msg);
        }
        }
    }

    class QueueHandler extends Handler {
        QueueHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            //Log.d("handleMessage from QueueHandler");
            synchronized(TransactionQueue.this) {
            int tag = msg.what;
            if (tag == REQUEST_CONSUME) {
            	if (mQueue != null && mQueue.size() >0) {
                    onRequestConsume();
				}
            } else if (tag == TRANSACTION_DONE) {
                onWorkingTransactionDone();
            }
            }
            //Log.d("handleMessage done");
        }

        private void onRequestConsume() {
            synchronized(TransactionQueue.this) {
                if (mWorkingTransaction != null) {
                    // there is already an ongoing transaction
                    return;
                }

                mWorkingTransaction = mQueue.poll();
                Log.d("transaction", "mWorkingTransaction : "+ mWorkingTransaction.toString());
                if (mWorkingTransaction != null) {
                    long timeout = mWorkingTransaction.getTimeout();
                    if (timeout != Transaction.TIMEOUT_NONE) {
                        // this request will not cause onConsumed although
                        // it already complete the transaction.
                        // we need request next transaction manually.
                        doneTransaction(timeout);
                    }

                    // found transaction
                    Log.d("ask consumer to transact one TRNSACTION, pending=" + mQueue.size());
                    
                    mConsumer.onTransact(mWorkingTransaction);
                    Log.d("onTransact done");
                }
            }
        }

        private void onWorkingTransactionDone() {
            synchronized(TransactionQueue.this) {
                mWorkingTransaction = null;
               	if (mQueue != null && mQueue.size() >0) {
                    requestConsume();
    			}
            }
            // finish one, request next transaction
        }
    }

    public interface Transaction {
        public final static long TIMEOUT_NONE = -999;

        public long getTimeout();
    }

    public interface Consumer<T extends Transaction> {
        public void onTransact(T transaction);
    }

}
