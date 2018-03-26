package Errors

import "errors"

const (
	NoError                = iota
	InternalServer         = iota
	Unauthorized           = iota
	ExistingAccount        = iota // returns username
	MissingParameter       = iota
	EmptyParameter         = iota
	InvalidUsername        = iota // returns username
	InvalidLogin           = iota
	InvalidConversationKey = iota
	InvalidMessageKey      = iota
	InvalidStatus          = iota
)

type Error struct {
	error string
	id    int
}

func New(error string, errType int) Error {
	return Error{error, errType}
}

func (err Error) Error() string {
	return err.error
}

func (err Error) Id() int {
	return err.id
}

const Tag = "ERROR:"

var (
	ErrInternalServer         = New("internal server error", InternalServer)
	ErrUnauthorized           = New("unauthorized", Unauthorized)
	ErrExistingAccount        = New("account already exists", ExistingAccount)
	ErrMissingParameter       = New("missing parameter", MissingParameter)
	ErrEmptyParameter         = New("empty parameter", EmptyParameter)
	ErrInvalidUsername        = New("invalid username", InvalidUsername)
	ErrInvalidLogin           = New("invalid login credentials", InvalidLogin)
	ErrInvalidConversationKey = New("invalid conversationKey", InvalidConversationKey)
	ErrInvalidMessageKey      = New("invalid messageKey", InvalidMessageKey)
	ErrInvalidStatus          = New("invalid Status", InvalidStatus)

	ErrInvalidPassword = errors.New("invalid password")
)
