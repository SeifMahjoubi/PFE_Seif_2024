import streamlit as st
import base64

# Chemin d'accès de la nouvelle image
background_image_path = "C:/Users/the cast/OneDrive/Bureau/Djezzy-app.webp"

try:
    with open(background_image_path, "rb") as img_file:
        background_image = img_file.read()
    image_loaded = True
except FileNotFoundError:
    st.error("Erreur : Impossible de trouver l'image. Veuillez vérifier le chemin d'accès.")
    image_loaded = False

# Interface de Connexion
st.title("Interface de Connexion")

# Variables pour les informations d'identification
correct_username = "Djezzy"
correct_password = "djezzy2023"

st.markdown("<br>", unsafe_allow_html=True)
st.markdown("**Nom d'utilisateur**")
username = st.text_input("")
st.markdown("<br>", unsafe_allow_html=True)
st.markdown("**Mot de passe**")
password = st.text_input("", type="password")

 

# Bouton de connexion
if st.button("Connexion"):
    if username == correct_username and password == correct_password:
        st.success("Connexion réussie ! Redirection en cours...")
        # Redirection vers http://localhost:8503/
        st.markdown('<meta http-equiv="refresh" content="2;URL=http://localhost:8503/" />', unsafe_allow_html=True)
    else:
        st.error("Nom d'utilisateur ou mot de passe incorrect")

if image_loaded:
    encoded_image = base64.b64encode(background_image).decode()
    st.markdown(
        f"""
        <style>
        .stApp {{
            background-image: url("data:image/webp;base64,{encoded_image}");
            background-size: cover;
            background-position: center;
            background-attachment: fixed; /* Fixer l'arrière-plan pour éviter le défilement */
        }}
        </style>
        """,
        unsafe_allow_html=True
    )
