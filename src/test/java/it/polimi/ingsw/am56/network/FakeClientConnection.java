package it.polimi.ingsw.am56.network;

import it.polimi.ingsw.am56.network.message.update.ServerMessage;

import java.util.ArrayList;
import java.util.List;

class FakeClientConnection implements ClientConnection {

    final List<ServerMessage> messages = new ArrayList<>();
    final List<String> errors = new ArrayList<>();
    boolean disconnected = false;

    @Override
    public void sendMessage(ServerMessage message) {
        messages.add(message);
    }

    @Override
    public void sendError(String errorDetails) {
        errors.add(errorDetails);
    }

    @Override
    public void disconnect() {
        disconnected = true;
    }

    <T extends ServerMessage> List<T> messagesOfType(Class<T> type) {
        return messages.stream().filter(type::isInstance).map(type::cast).toList();
    }

    boolean hasMessageOfType(Class<? extends ServerMessage> type) {
        return messages.stream().anyMatch(type::isInstance);
    }
}
