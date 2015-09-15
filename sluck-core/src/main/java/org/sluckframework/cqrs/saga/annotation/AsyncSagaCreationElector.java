package org.sluckframework.cqrs.saga.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 选择器,选择创建 saga 的 event processor
 *
 * Author: sunxy
 * Created: 2015-09-15 21:52
 * Since: 1.0
 */
public class AsyncSagaCreationElector {

    private static final Logger logger = LoggerFactory.getLogger(AsyncSagaCreationElector.class);

    private final ReentrantLock votingLock = new ReentrantLock();
    private final Condition allVotesCast = votingLock.newCondition();

    // guarded by "votingLock"
    private int castVotes = 0;
    private volatile boolean invocationDetected = false;

    public boolean waitForSagaCreationVote(final boolean didInvocation, final int totalVotesExpected,
                                           final boolean isSagaOwner) {
        votingLock.lock();
        try {
            invocationDetected = invocationDetected || didInvocation;
            castVotes++;
            while (isSagaOwner && !invocationDetected && castVotes < totalVotesExpected) {
                try {
                    allVotesCast.await();
                } catch (InterruptedException e) {
                    // interrupting this process is not supported.
                    logger.warn("This thread has been interrupted, but the interruption has "
                            + "been ignored to prevent loss of information.");
                }
            }
            if (isSagaOwner) {
                return !invocationDetected;
            }
            allVotesCast.signalAll();
        } finally {
            votingLock.unlock();
        }
        return false;
    }

    public void clear() {
        votingLock.lock();
        try {
            castVotes = 0;
            invocationDetected = false;
        } finally {
            votingLock.unlock();
        }
    }
}
