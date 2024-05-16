import streamlit as st
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error
from sklearn.preprocessing import OneHotEncoder
import xgboost as xgb

# Charger les données des offres par emplacement
fichier_offres = r"C:\Users\the cast\OneDrive\Bureau\POS_DATA\predection_offre_pdv.csv"
donnees = pd.read_csv(fichier_offres)

# Interface utilisateur Streamlit
st.title("Analyse des transactions")

# Filtrage des données par région, wilaya, commune et point de vente
selected_region = st.sidebar.selectbox("Région", donnees['Region'].unique())
donnees = donnees[donnees['Region'] == selected_region]

selected_wilaya = st.sidebar.selectbox("Wilaya", donnees['Wilaya'].unique())
donnees = donnees[donnees['Wilaya'] == selected_wilaya]

selected_commune = st.sidebar.selectbox("Commune", donnees['Commune'].unique())
donnees = donnees[donnees['Commune'] == selected_commune]

selected_pdv = st.sidebar.selectbox("Point de vente (PDV)", donnees['Code_PDV'].unique())
donnees = donnees[donnees['Code_PDV'] == selected_pdv]

# Diviser les données en fonctionnalités (X) et la cible (y)
X = donnees.drop(columns=['Nombre_offres'])
y = donnees['Nombre_offres']

# Encodage one-hot des colonnes catégorielles
cat_columns = ['Region', 'Wilaya', 'Commune', 'Code_PDV', 'brand_name']
X_encoded = pd.get_dummies(X, columns=cat_columns)

# Diviser les données en ensembles d'entraînement et de test
X_train, X_test, y_train, y_test = train_test_split(X_encoded, y, test_size=0.2, random_state=42)

# Entraîner le modèle XGBoost
model = xgb.XGBRegressor()
model.fit(X_train, y_train)

# Faire des prédictions sur l'ensemble de test
predictions = model.predict(X_test)

# Création de la grille de graphiques
fig, axes = plt.subplots(nrows=2, ncols=2, figsize=(12, 10))

# Utiliser le modèle pour prédire les nombres d'offres par mois
predictions_par_mois = model.predict(X_encoded)

# Remplacer les valeurs réelles par les valeurs prédites dans le DataFrame donnees
donnees['Nombre_offres'] = predictions_par_mois

# Graphique des nombres de transactions par mois  
plt.subplot(2, 2, 1)
donnees.groupby('Mois')['Nombre_offres'].sum().plot(kind='bar')
plt.title('Nombre de transactions par mois')
plt.xlabel('Mois')
plt.ylabel('Nombre de transactions')

# Graphique des cinq offres les plus vendues  
plt.subplot(2, 2, 2)
top5_offres = donnees['brand_name'].value_counts().head().index.tolist()
predictions_top5 = pd.Series(predictions, index=X_test.index)
predictions_top5.value_counts().head().plot(kind='pie', autopct='%1.1f%%', startangle=140, labels=top5_offres)
plt.title('Cinq offres les plus vendues  ')

# Graphique des cinq offres les moins vendues  
plt.subplot(2, 2, 3)
bottom5_offres = donnees['brand_name'].value_counts().tail().index.tolist()
predictions_bottom5 = pd.Series(predictions, index=X_test.index)
predictions_bottom5.value_counts().tail().plot(kind='pie', autopct='%1.1f%%', startangle=140, labels=bottom5_offres)
plt.title('Cinq offres les moins vendues  ')

# Prédire les offres restantes
offres_restantes_predictions = model.predict(X_encoded[~X_encoded.index.isin(X_test.index)])

# Créer un DataFrame pour les offres restantes  
offres_restantes_df = pd.DataFrame({'predictions': offres_restantes_predictions})

# Graphique des offres restantes  
plt.subplot(2, 2, 4)
donnees['brand_name'].value_counts().plot(kind='pie', autopct='%1.1f%%', startangle=140)
plt.title('Offres restantes  ')
plt.ylabel('')

# Affichage de la grille de graphiques  
plt.tight_layout()
st.pyplot(fig)
