from flask_wtf import FlaskForm
from wtforms import (StringField, PasswordField, BooleanField, FileField)
from wtforms.validators import InputRequired, Length, Email

class LoginForm(FlaskForm):
    email = StringField('email', validators=[Email()])
    password = PasswordField('password', validators=[InputRequired()])
    remember_me = BooleanField('remember_me')

class SignupForm(FlaskForm):
    id = StringField('id', validators=[InputRequired()])
    name = StringField('name', validators=[InputRequired()])
    email = StringField('email', validators=[InputRequired(), Email()])
    password = PasswordField('password', validators=[InputRequired()])

class DialogueForm(FlaskForm):
    dialogueId = StringField('dialogueId', validators=[InputRequired()])



