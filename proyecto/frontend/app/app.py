from flask import Flask, render_template, send_from_directory, url_for, request, redirect
from flask_login import LoginManager, login_manager, current_user, login_user, login_required, logout_user
import requests
import os

# Usuarios
from models import users, User

# Login
from forms import LoginForm, SignupForm

app = Flask(__name__, static_url_path='')
login_manager = LoginManager()
login_manager.init_app(app) # Para mantener la sesión

# Configurar el secret_key. OJO, no debe ir en un servidor git público.
# Python ofrece varias formas de almacenar esto de forma segura, que
# no cubriremos aquí.
app.config['SECRET_KEY'] = 'qH1vprMjavek52cv7Lmfe1FoCexrrV8egFnB21jHhkuOHm8hJUe1hwn7pKEZQ1fioUzDb3sWcNK1pJVVIhyrgvFiIrceXpKJBFIn_i9-LTLBCc4cqaI3gjJJHU6kxuT8bnC7Ng'


@app.route('/static/<path:path>')
def serve_static(path):
    return send_from_directory('static', path)

@app.route('/')
def index():
    return render_template('index.html')


@app.route('/signup', methods=['GET', 'POST'])
def signup():
    if current_user.is_authenticated:
        return redirect(url_for('index'))
    else:
        error = None
        form = SignupForm(None if request.method != 'POST' else request.form)
        if request.method == "POST" and form.validate():
            user_id= len(users)
            user = User(user_id,form.name.data.encode('utf-8'), form.email.data.encode('utf-8'), form.password.data.encode('utf-8'))
            #TODO   response = requests.post("backend-server/login-attempt", json = user)
            #if (response!=ok) error: xxx
            #else:
            users.append(user)
            return redirect(url_for('login'))
        return render_template('signup.html', form=form,  error=error)



@app.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('index'))
    else:
        error = None
        form = LoginForm(None if request.method != 'POST' else request.form)
        if request.method == "POST" and form.validate():
            user= User.get_user(form.email.data.encode('utf-8'))
            if user and user.check_password(form.password.data.encode('utf-8')):
                login_user(user, remember=form.remember_me.data)
                return redirect(url_for('profile'))
            else:
                error = "Usuario o contraseña incorrecto"
        return render_template('login.html', form=form, error=error)

@app.route('/profile')
@login_required
def profile():
    current_user.name = current_user.name.decode('utf-8') if isinstance(current_user.name, bytes) else current_user.name
    current_user.email = current_user.email.decode('utf-8') if isinstance(current_user.email, bytes) else current_user.email

    return render_template('profile.html')

@app.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('index'))

@login_manager.user_loader
def load_user(user_id):
    for user in users:
        if user.id == int(user_id):
            return user
    return None

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=int(os.environ.get('PORT', 5010)))
