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
package org.jgroups.samples.client;

import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.gwt.mosaic.ui.client.tree.FastTree;
import org.gwt.mosaic.ui.client.tree.FastTreeItem;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.framework.MessageBus;

import java.util.List;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Oct 11, 2010
 */
public class UserPanel extends LayoutPanel {

    private FastTree tree;
    private FastTreeItem root;

    private MessageBus bus = ErraiBus.get();

    public UserPanel() {
        super();


        tree = new FastTree();
        root = tree.addItem("Chat");
        this.add(tree);

        bus.subscribe("UserManagement", new MessageCallback()
        {
            public void callback(Message message) {

                if("update".equals(message.getCommandType()))
                {
                    List<String> usernames = (List<String>)
                            message.get(List.class, "currentUsers");

                    root.removeItems();
                    for(String name : usernames)
                        root.addItem(name);

                    root.setState(true);
                }
            }
        });
    }


}
