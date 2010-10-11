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
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Service;

import java.util.ArrayList;
import java.util.List;


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
public class ChatService {

    @Inject
    MessageBus bus;

    private List<String> usernames = new ArrayList<String>();

    public ChatService() {
        // register callbacks with mbean

        // jndi lookup
        // add callback
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
            usernames.add(name);

            // all the others get updated
            MessageBuilder.createMessage()
                    .toSubject("UserManagement")
                    .command("update")
                    .with("currentUsers", usernames)
                    .noErrorHandling().sendNowWith(bus);
        }

    }

    @Command("broadcast")
    public void broadcast(Message message)
    {
        System.out.println("Broadcast: " + message.get(String.class, "text"));

        // lookup chat bean

        // invoke method        

        MessageBuilder.createMessage()
                .toSubject("ChatClient")
                .signalling()
                .with("text", message.get(String.class, "text"))
                .done().sendNowWith(bus);
    }
}
