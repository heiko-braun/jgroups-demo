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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.protocols.MessageParts;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Oct 11, 2010
 */
public class LoginPanel extends LayoutPanel {

    private TextBox username;
    private MessageBus bus = ErraiBus.get();

    public LoginPanel() {

        super(new BoxLayout(BoxLayout.Orientation.VERTICAL));
        username = new TextBox();
        username.addKeyDownHandler(new KeyDownHandler()
        {
            public void onKeyDown(KeyDownEvent keyDownEvent) {
                if(keyDownEvent.getNativeKeyCode() == 13)
                    login();
            }
        });
        add(new Label("Username"));
        add(username);
        add(new Button("Login", new ClickHandler()
        {
            public void onClick(ClickEvent clickEvent) {
                login();
            }
        }
        ));

        username.setFocus(true);
    }

    private void login()
    {
        MessageBuilder.createMessage()
                .toSubject("ChatService")
                .command("login")
                .with(MessageParts.ReplyTo, "ChatClient")
                .with("username", username.getText())
                .done().sendNowWith(bus);
    }
}
