package application;

import connection.serverMessages.ServerMessage;

abstract public class ViewController {
    abstract void notification(ServerMessage message);
}