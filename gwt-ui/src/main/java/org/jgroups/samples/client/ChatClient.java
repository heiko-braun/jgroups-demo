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
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import org.gwt.mosaic.ui.client.DeckLayoutPanel;
import org.gwt.mosaic.ui.client.MessageBox;
import org.gwt.mosaic.ui.client.StackLayoutPanel;
import org.gwt.mosaic.ui.client.layout.*;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Oct 7, 2010
 */
public class ChatClient extends DeckLayoutPanel {

    MessageBus bus = ErraiBus.get();
   
    private TextArea textArea;
    private TextBox input;
    private String username;
    
    public ChatClient() {
        super();
        init();
    }

    public void init() {

        this.add(new LoginPanel());

        textArea = new TextArea();
        textArea.setEnabled(false);        
        input = new TextBox();
        input.addKeyDownHandler(new KeyDownHandler()
        {
            public void onKeyDown(KeyDownEvent keyDownEvent) {
                if(keyDownEvent.getNativeKeyCode() == 13)
                    sendMessage();
            }
        });

        LayoutPanel chatPanel = new LayoutPanel(new BorderLayout());
        chatPanel.add(textArea);

        LayoutPanel submitPanel = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));
        submitPanel.setPadding(0);
        submitPanel.add(input, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));
        submitPanel.add(new Button("Send", new ClickHandler()
        {
            public void onClick(ClickEvent clickEvent) {
                sendMessage();
            }
        }));

        chatPanel.add(submitPanel, new BorderLayoutData(BorderLayout.Region.SOUTH, "50px"));

        this.add(chatPanel);
        this.showWidget(0);

        // login callback
        bus.subscribe("ChatClient", new MessageCallback()
        {
            public void callback(Message message) {

                if("loginSuccess".equals(message.getCommandType()))
                {
                    ChatClient.this.username = message.get(String.class, "username");
                    ChatClient.this.showWidget(1);
                    ChatClient.this.layout();
                }
                else if("loginFailure".equals(message.getCommandType()))
                {
                    MessageBox.alert("Error", "Username already in use!");
                    
                }
                else
                {
                    String prev = textArea.getText()+"\n";
                    textArea.setText(prev+message.get(String.class, "text"));
                }
            }
        });

    }

    private void sendMessage() {
        MessageBuilder.createMessage()
                .toSubject("ChatService")
                .command("broadcast")
                .with("text", username + ": " +input.getText())
                .done().sendNowWith(bus);
        input.setText("");
    }
}

