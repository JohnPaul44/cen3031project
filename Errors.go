package main

import "errors"

const (
	ErrorInternalServer         = iota
	ErrorInvalidJSON            = iota
	ErrorUnauthorized           = iota
	ErrorExistingAccount        = iota // returns username
	ErrorMissingParameter       = iota
	ErrorEmptyParameter         = iota
	ErrorInvalidUsername        = iota // returns username
	ErrorInvalidLogin           = iota
	ErrorInvalidConversationKey = iota
	ErrorInvalidMessageKey      = iota
	ErrorInvalidStatus          = iota
)

type ServerError struct {
	error string
	id    int
}

func NewError(error string, errType int) ServerError {
	return ServerError{error, errType}
}

func (err ServerError) Error() string {
	return err.error
}

func (err ServerError) Id() int {
	return err.id
}

const ErrorTag = "ERROR:"

var (
	ErrInternalServer         = NewError("internal server error", ErrorInternalServer)
	ErrInvalidJSON            = NewError("invalid JSON", ErrorInvalidJSON)
	ErrUnauthorized           = NewError("unauthorized", ErrorUnauthorized)
	ErrExistingAccount        = NewError("account already exists", ErrorExistingAccount)
	ErrMissingParameter       = NewError("missing parameter", ErrorMissingParameter)
	ErrEmptyParameter         = NewError("empty parameter", ErrorEmptyParameter)
	ErrInvalidUsername        = NewError("invalid username", ErrorInvalidUsername)
	ErrInvalidLogin           = NewError("invalid login credentials", ErrorInvalidLogin)
	ErrInvalidConversationKey = NewError("invalid conversationKey", ErrorInvalidConversationKey)
	ErrInvalidMessageKey      = NewError("invalid messageKey", ErrorInvalidMessageKey)
	ErrInvalidStatus          = NewError("invalid Status", ErrorInvalidStatus)

	ErrInvalidPassword = errors.New("invalid password")
)
