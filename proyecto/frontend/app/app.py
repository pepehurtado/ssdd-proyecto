from flask import Flask, render_template, send_from_directory, url_for, request, redirect
from flask_login import LoginManager, login_manager, current_user, login_user, login_required, logout_user
import requests
import os
import logging
from requests.exceptions import RequestException, JSONDecodeError
from datetime import datetime
from models import users, User
from forms import LoginForm, SignupForm, DialogueForm

#prueba
app = Flask(__name__, static_url_path='')
login_manager = LoginManager()
login_manager.init_app(app)

logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)

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

    form = SignupForm(request.form)
    error = None

    if request.method == "POST":
        logger.debug("Iniciando registro")
        if form.validate():
            user = {
                'id': form.id.data,
                'name': form.name.data,  
                'password': form.password.data,
                'email': form.email.data
            }
            logger.debug(f"Enviando registro: {user}")
            try:
                response = requests.post('http://backend-rest:8080/Service/u/register', json=user)
                if response.ok:
                    response_data = response.json()
                    logger.debug(f"Respuesta del servidor: {response_data}")
                    if response.status_code == 201:
                        logger.debug("Registro exitoso.")
                        return redirect(url_for('login'))
                    else:
                        logger.debug("Registro fallido")
                        error = "Registro fallido"
                else:
                    logger.debug(f"Error en la solicitud: {response.status_code} - {response.text}")
                    error = "Error en la solicitud"
            except requests.exceptions.RequestException as e:
                logger.debug(f"Excepción al enviar la solicitud: {e}")
                error = "Excepción al enviar la solicitud"
        else:
            logger.debug("Formulario no válido")
            error = "Formulario no válido"

    return render_template('signup.html', form=form, error=error)

@app.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('index'))
    
    error = None
    form = LoginForm(None if request.method != 'POST' else request.form)
    
    if request.method == "POST" and form.validate():
        usuario = {
            'email': form.email.data,
            'password': form.password.data
        }
        
        response = requests.post('http://backend-rest:8080/Service/checkLogin', json=usuario)
        logger.debug("Iniciando check log : %s", response)
        
        if response.ok:
            datos_usuario = response.json()
            logging.debug("Datos recibidos: %s", datos_usuario)
            
            # Buscar si el usuario ya está en la lista global `users`
            user = next((u for u in users if u.id == datos_usuario['id']), None)
            if user:
                user.visits = datos_usuario['visits']
            else:
                user = User(datos_usuario['id'], datos_usuario['name'], form.email.data.encode('utf-8'), form.password.data.encode('utf-8'), datos_usuario['visits'])
                users.append(user)

            # Login del usuario
            login_user(user, remember=form.remember_me.data)
            return redirect(url_for('profile'))
        else:
            error = "Usuario o contraseña incorrectos. "
    
    return render_template('login.html', form=form, error=error)

@app.route('/dialogue', methods=['GET', 'POST'])
@login_required
def dialogue():
    error = None
    form = DialogueForm(None if request.method != 'POST' else request.form)
    username = current_user.id
    dialogues = []
    try:
        response = requests.get(f'http://backend-rest:8080/Service/u/{username}/dialogue')
        logger.debug(f"GET /dialogue response status: {response.status_code}")
        logger.debug(f"GET /dialogue response text: {response.text}")
        if response.status_code == 200:
            try:
                dialogues = response.json()
                logger.debug(f"GET /dialogue JSON: {dialogues}")
            except ValueError as e:
                logger.error("Error de decodificación JSON en respuesta GET")
                logger.debug(f"ValueError: {e}")
        else:
            logger.debug(f"Error en la solicitud GET: {response.status_code} - {response.text}")
    except requests.RequestException as e:
        logger.error(f"Excepción al enviar la solicitud GET: {e}")

    if request.method == "POST" and form.validate():
        dialogue = {
            'dialogueId': form.dialogueId.data,
        }
        try:
            response = requests.post(f'http://backend-rest:8080/Service/u/{username}/dialogue', json=dialogue)
            logger.debug(f"POST /dialogue response status: {response.status_code}")
            logger.debug(f"POST /dialogue response text: {response.text}")
            if response.status_code == 201:
                return redirect(url_for('dialogue'))
            else:
                error = response.text
                logger.debug(f"Error en la solicitud POST: {response.status_code} - {response.text}")
        except requests.RequestException as e:
            logger.error(f"Excepción al enviar la solicitud POST: {e}")
            error = "Excepción al enviar la solicitud"

    return render_template('dialogue.html', form=form, error=error, dialogues=dialogues)



@app.route('/dialogue/<dialogueId>/update_dialogue', methods=['GET', 'POST', 'PUT'])
@login_required
def update_dialogue(dialogueId):
    error = None
    username = current_user.id

    if request.method == "GET":
        try:
            response = requests.get(f'http://backend-rest:8080/Service/u/{username}/dialogue/{dialogueId}')
            logger.debug(f"GET /dialogue/{dialogueId} response status: {response.status_code}")
            logger.debug(f"GET /dialogue/{dialogueId} response text: {response.text}")
            if response.status_code == 200:
                try:
                    dialogue = response.json()
                    logger.debug(f"GET /dialogue/{dialogueId} JSON:")
                except ValueError as e:
                    logger.error("Error de decodificación JSON en respuesta GET")
                    logger.debug(f"ValueError: {e}")
            else:
                logger.debug(f"Error en la solicitud GET: {response.status_code} - {response.text}")
        except requests.RequestException as e:
            logger.error(f"Excepción al enviar la solicitud GET: {e}")

        return render_template('dialogue_id.html', error=error, dialogueId=dialogueId)

    if request.method in ["POST", "PUT"]:
        new_dialogue = {
            'dialogueId': request.form['dialogueId']
        }
        
        try:
            response = requests.put(f'http://backend-rest:8080/Service/u/{username}/dialogue/{dialogueId}', json=new_dialogue)
            logger.debug(f"PUT /dialogue/{dialogueId}/{request.form['dialogueId']} response status: {response.status_code}")
            logger.debug(f"PUT /dialogue/{dialogueId} response text: {response.text}")
            logger.debug(f'LA PETICION ES:    http://backend-rest:8080/Service/u/{username}/dialogue/{dialogueId}' + str(new_dialogue))
            if response.status_code == 200:
                return redirect(url_for('dialogue'))
            else:
                error = "Status code fallido"
                logger.debug(f"Error en la solicitud PUT: {response.status_code} - {response.text}")
        except requests.RequestException as e:
            logger.error(f"Excepción al enviar la solicitud PUT: {e}")
            error = "Excepción al enviar la solicitud"

    return render_template('dialogue_id.html', error=error, dialogueId=dialogueId)

@app.route('/dialogue/<dialogueId>/delete_dialogue', methods=['POST'])
@login_required
def delete_dialogue(dialogueId):
    error = None
    username = current_user.id

    try:
        response = requests.delete(f'http://backend-rest:8080/Service/u/{username}/dialogue/{dialogueId}')
        logger.debug(f"DELETE /dialogue/{dialogueId} response status: {response.status_code}")
        logger.debug(f"DELETE /dialogue/{dialogueId} response text: {response.text}")
        if response.status_code == 200:
            return redirect(url_for('dialogue'))
        else:
            error = "Error al eliminar el diálogo"
            logger.debug(f"Error en la solicitud DELETE: {response.status_code} - {response.text}")
    except requests.RequestException as e:
        logger.error(f"Excepción al enviar la solicitud DELETE: {e}")
        error = "Excepción al enviar la solicitud"

    return render_template('dialogue_id.html', error=error, dialogueId=dialogueId)


@app.route('/dialogue/<dialogueId>', methods=['GET'])
@login_required
def get_dialogue(dialogueId):
    error = None
    username = current_user.id

    try:
        response = requests.get(f'http://backend-rest:8080/Service/u/{username}/dialogue/{dialogueId}')
        logger.debug(f"GET /dialogue/{dialogueId} response status: {response.status_code}")
        logger.debug(f"GET /dialogue/{dialogueId} response text: {response.text}")
        if response.status_code == 200:
            try:
                dialogue = response.json()
                logger.debug(f"GET /dialogue/{dialogueId} JSON: {dialogue}")
            except ValueError as e:
                logger.error("Error de decodificación JSON en respuesta GET")
                logger.debug(f"ValueError: {e}")
                dialogue = None
        else:
            logger.debug(f"Error en la solicitud GET: {response.status_code} - {response.text}")
            dialogue = None
    except requests.RequestException as e:
        logger.error(f"Excepción al enviar la solicitud GET: {e}")
        dialogue = None

    return render_template('dialogue_detail.html', error=error, dialogue=dialogue)



@app.route('/profile')
@login_required
def profile():
    current_user.name = current_user.name.decode('utf-8') if isinstance(current_user.name, bytes) else current_user.name
    current_user.email = current_user.email.decode('utf-8') if isinstance(current_user.email, bytes) else current_user.email
    logger.debug(f"Visitas: {current_user.visits}")
    current_user.visits = current_user.visits.decode('utf-8') if isinstance(current_user.visits, bytes) else current_user.visits
    return render_template('profile.html')


@app.route('/delete_user', methods=['POST'])
@login_required
def delete_user():
    username = current_user.id
    logger.debug(f"Attempting to delete user: {username}")
    url = f'http://backend-rest:8080/Service/u/{username}'
    logger.debug(f"URL for delete requesttttttttttt: {url}")
    
    try:
        response = requests.delete(url)
        logger.debug(f"Response from delete request: {response.status_code} - {response.text}")
        
        if response.status_code == 204:
            logout_user()
            return redirect(url_for('index'))
        elif response.status_code == 404:
            error = "No se ha encontrado el usuario"
        else:
            error = "ERROR: eliminar usuario"
    except requests.exceptions.RequestException as e:
        logger.error(f"Exception during delete request: {e}")
        error = "Excepción al enviar la solicitud"
    return render_template('profile.html', error=error)

@app.route('/dialogue/<dialogueId>/chat', methods=['GET'])
@login_required
def chat(dialogueId):
    error = None
    username = current_user.id

    try:
        response = requests.get(f'http://backend-rest:8080/Service/u/{username}/dialogue/{dialogueId}')
        logger.debug(f"GET /dialogue/{dialogueId} response status: {response.status_code}")
        logger.debug(f"GET /dialogue/{dialogueId} response text: {response.text}")
        if response.status_code == 200:
            try:
                dialogue = response.json()
                logger.debug(f"GET /dialogue/{dialogueId} JSON: {dialogue}")
            except ValueError as e:
                logger.error("Error de decodificación JSON en respuesta GET")
                logger.debug(f"ValueError: {e}")
                dialogue = None
        else:
            logger.debug(f"Error en la solicitud GET: {response.status_code} - {response.text}")
            dialogue = None
    except requests.RequestException as e:
        logger.error(f"Excepción al enviar la solicitud GET: {e}")
        dialogue = None

    return render_template('chat.html', error=error, dialogue=dialogue)



@app.route('/chat/<dialogueId>/send_message', methods=['GET', 'POST'])
@login_required
def send_message(dialogueId):
    error = None
    username = current_user.id
    dialogue_data = None  # Variable para almacenar datos del diálogo

    if request.method == "POST":
        responseGet = requests.get(f'http://backend-rest:8080/Service/u/{username}/dialogue/{dialogueId}')
        logger.debug(f"GET /dialogue/{dialogueId} response status: {responseGet.status_code}")
        logger.debug(f"GET /dialogue/{dialogueId} response text: {responseGet.text}")

        if responseGet.status_code == 200:
            dialogue_data = responseGet.json()  # Guardar datos del diálogo
            nextUrl = dialogue_data.get('nextUrl')
            timestamp = datetime.utcnow().strftime('%Y-%m-%dT%H:%M:%S.%f')
            logger.debug(f"Timestamp: {timestamp}")
            message = request.form.get('message')
            logger.debug(f"Mensaje de CONSULTA: {message}")
            question = {'prompt': message, 'timestamp': timestamp}
            responsePost = requests.post(f'http://backend-rest:8080/Service/u/{username}{nextUrl}', json=question)
            logger.debug(responsePost)

            if responsePost.status_code == 201:
                return redirect(url_for('chat', dialogueId=dialogueId))
            elif responsePost.status_code == 204:
                error = "La solicitud fue denegada, se encuentra en estado BUSY"
                logger.debug(f"Solicitud denegada, se encuentra en estado BUSY: {responsePost.status_code} - {responsePost.text}")
            elif responsePost.status_code == 409:
                error = "El dialogo se encuentra finalizado y no se puede enviar mensajes"
                logger.debug(f"Solicitud denegada, se encuentra en estado BUSY: {responsePost.status_code} - {responsePost.text}")
            else:
                error = "Error al enviar el mensaje"
                logger.debug(f"Error en la solicitud POST: {responsePost.status_code} - {responsePost.text}")
        else:
            error = "Error al cargar el diálogo"
            logger.debug(f"Error en la solicitud GET: {responseGet.status_code} - {responseGet.text}")
    
    # Renderizar la página con los datos del diálogo (si están disponibles)
    return render_template('chat.html', error=error, dialogue=dialogue_data)



@app.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('index'))
@login_manager.user_loader
def load_user(user_id):
    for user in users:
        if user.id == user_id:
            return user
    return None

if __name__ == '__main__':
    app.logger.setLevel(logging.DEBUG)
    app.run(debug=True, host='0.0.0.0', port=int(os.environ.get('PORT', 5010)))
