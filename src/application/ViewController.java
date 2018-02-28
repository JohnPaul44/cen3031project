package application;

import connection.serverMessages.ServerMessage;

abstract public class ViewController {
    public abstract void notification(ServerMessage message);
}