/*
 * (c) 2003-2014 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.api.transaction;

import org.mule.api.MuleContext;

/**
 * Create an unbound transaction, to be bound and started by other parts of the transaction framework.
 */
public interface UnboundTransactionFactory
{
    Transaction createUnboundTransaction(MuleContext muleContext) throws TransactionException;
}