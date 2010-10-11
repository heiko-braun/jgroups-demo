/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jgroups.samples.server;

import com.google.inject.Inject;
import org.jboss.Chat;
import org.jboss.ChatCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Service;

import javax.naming.InitialContext;
import java.util.HashSet;
import java.util.Set;


/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@Service
public class ChatService implements ChatCallback {

    @Inject
    MessageBus bus;

    private Set<String> usernames = new HashSet<String>();
    private Chat chat = null;

    public ChatService() {
        // register callbacks with chat service
        try {
            InitialContext ctx = new InitialContext();
            Chat chat = (Chat)ctx.lookup("jgroups/Chat");
            chat.addListener(this);
        } catch (Exception e) {
            System.out.println("Seems to be running in GWT hosted mode ...");
        }
    }

    /**
     * Cluster callback
     * @param msg
     */
    public void postMessage(String msg) {

        // received from cluster, notify web clients
        messageToClients(msg);
    }

    /**
     * Cluster callback
     * @param name
     * @param joined
     */
    public void postMemberJoinedOrLeft(String name, boolean joined) {
        // received from cluster, notify web clients
        if(joined)
            newUser(name);
    }

    /**
     * register new user and notify web clients
     * @param name
     */
    private void newUser(String name)
    {
        usernames.add(name);

        // all the others get updated
        MessageBuilder.createMessage()
                .toSubject("UserManagement")
                .command("update")
                .with("currentUsers", usernames)
                .noErrorHandling().sendNowWith(bus);
    }

    @Command("login")
    public void login(Message message)
    {
        String name = message.get(String.class, "username");

        boolean nameTaken = usernames.contains(name);

        // the client which logs in
        String command = nameTaken ? "loginFailure" : "loginSuccess";
        MessageBuilder.createConversation(message)
                .subjectProvided()
                .command(command)
                .with("username", name)
                .done().sendNowWith(bus);

        if(!nameTaken)
        {
            // web clients
            newUser(name);

            // cluster members
            if(chat!=null)
                chat.memberJoinedOrLeft(name, true);
        }

    }

    @Command("broadcast")
    public void broadcast(Message message)
    {
        String payload = message.get(String.class, "text");
        System.out.println("Broadcast: " + payload);

        // notify both web clients and cluster members
        messageToClients(payload);
        messageToMembers(payload);
    }

    /**
     * Notify cluster members
     * @param text
     */
    public void messageToMembers(String text)
    {
        if(null==chat) return;

        chat.postMessage(text);

    }

    /**
     * Notify web clients
     * @param text
     */
    public void messageToClients(String text)
    {
        // web clients
        MessageBuilder.createMessage()
                .toSubject("ChatClient")
                .signalling()
                .with("text", text)
                .done().sendNowWith(bus);

    }
}
