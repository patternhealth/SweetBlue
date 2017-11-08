package com.idevicesinc.sweetblue.rx.exception;


import com.idevicesinc.sweetblue.utils.Event;

/**
 * Base Exception class for holding event instance when an error occurs (a read/write/connect/bond fails)
 */
public abstract class EventException extends Exception
{

    private final Event m_event;


    public <T extends Event> EventException(T event)
    {
        super();
        m_event = event;
    }

    /**
     * Returns the event instance holding information as to what went wrong.
     */
    public <T extends Event> T getEvent()
    {
        return (T) m_event;
    }

}
