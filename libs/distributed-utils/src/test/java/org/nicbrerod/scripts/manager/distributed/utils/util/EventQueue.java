package org.nicbrerod.scripts.manager.distributed.utils.util;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;

/**
 * Custom AbstractQueue implementation used to process a received message automatically
 * 
 * @see AbstractQueue
 * @see Consumer
 */
@RequiredArgsConstructor
public class EventQueue<E> extends AbstractQueue<E> {

    /**
     * Delegate object to "extend" from it. As {@link Abstractqueue} forces us to 
     * implement all the necessary methods, we use a queue of some specific 
     * type to "delegate" the methods that we do not want to re-implement.
     */
    private final Queue<E> delegate;

    /**
     * List of listeners to call when a new message has been received
     */
    private final List<Consumer<E>> listeners = new ArrayList<>();

    /**
     * Adds a new listener to this queue
     * @param listener Consumer object to call when new message is received, passing it as parameter
     * @see Consumer
     */
    public void registerListener(Consumer<E> listener) {
        this.listeners.add(listener);
    }

    /**
     * Method to insert a new message and process it automatically. When message has been processed, message is automatically
     * removed from queue.
     */
    @Override
    public boolean offer(E e) {
        boolean ok = false;

        try {
            if (delegate.offer(e)) {
                listeners.forEach(litener -> litener.accept(e));
                ok = true;
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            delegate.remove();
        }

        return ok;
    }

    @Override
    public E peek() {
        return delegate.peek();
    }

    @Override
    public E poll() {
        return delegate.poll();
    }

    @Override
    public Iterator<E> iterator() {
        return delegate.iterator();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    
}
