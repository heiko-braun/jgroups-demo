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
import com.google.gwt.user.client.ui.*;
import org.gwt.mosaic.ui.client.DeckLayoutPanel;
import org.gwt.mosaic.ui.client.MessageBox;
import org.gwt.mosaic.ui.client.layout.BorderLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Oct 7, 2010
 */
@LoadTool(name="Chat", group="Views")
public class ChatClient implements WidgetProvider {

    private DeckLayoutPanel deck;
    private TextBox username;

    MessageBus bus = ErraiBus.get();

    private TextArea textArea;
    private TextBox input;

    public void provideWidget(ProvisioningCallback callback) {
        deck = new DeckLayoutPanel();

        LayoutPanel login = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
        username = new TextBox();
        login.add(new Label("Username"));
        login.add(username);
        login.add(new Button("Login", new ClickHandler()
        {
            public void onClick(ClickEvent clickEvent) {
                MessageBuilder.createMessage()
                        .toSubject("ChatService")
                        .command("login")
                        .with(MessageParts.ReplyTo, "ChatClient")
                        .with("username", username.getText())
                        .done().sendNowWith(bus);

            }
        }
        ));

        deck.add(login);

        textArea = new TextArea();
        textArea.setCharacterWidth(50);
        textArea.setVisibleLines(5);
        input = new TextBox();
        LayoutPanel chatPanel = new LayoutPanel(new BorderLayout());
        LayoutPanel submitPanel = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));
        submitPanel.add(input, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));
        submitPanel.add(new Button("Send", new ClickHandler()
        {
            public void onClick(ClickEvent clickEvent) {
                MessageBuilder.createMessage()
                        .toSubject("ChatService")
                        .command("broadcast")
                        .with("message", input.getText())
                        .done().sendNowWith(bus);
                input.setText("");
            }
        }));
        chatPanel.add(textArea);
        chatPanel.add(submitPanel);

        deck.add(chatPanel);
        deck.showWidget(0);

        // login callback
        bus.subscribe("ChatClient", new MessageCallback()
        {
            public void callback(Message message) {

                if("loginSuccess".equals(message.getCommandType()))
                {
                    MessageBox.confirm("Login Successful", "Have fun!",
                            new MessageBox.ConfirmationCallback()
                            {
                                public void onResult(boolean b) {
                                    deck.showWidget(1);
                                }
                            });

                }
                else
                {
                    String prev = textArea.getText()+"\n";
                    textArea.setText(prev+message.get(String.class, "message"));
                }
            }
        });

        callback.onSuccess(deck);
    }
}

