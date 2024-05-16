import streamlit as st
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor
from xgboost import XGBRegressor
from sklearn.linear_model import LinearRegression
from sklearn.svm import SVR
from sklearn.metrics import r2_score
import matplotlib.pyplot as plt

def main():
    st.title("Évaluation des Performances des Modèles de Prédiction")
 
    # Charger les données
    data = pd.read_csv("C:/Users/the cast/OneDrive/Bureau/POS_DATA/Analyse Prédictive et Classification .csv")

    # Filtrer les données pour ne garder que les lignes avec des valeurs valides de Somme_transactions
    data = data.dropna(subset=['Somme_transactions'])

    # Sélectionner les variables d'entrée et de sortie
    X = data[['Mois']].values
    y_transactions = data['Nombre_transactions'].values
    y_somme = data['Somme_transactions'].values

    # Diviser les données en ensembles de formation et de test
    X_train, X_test, y_train_transactions, y_test_transactions = train_test_split(X, y_transactions, test_size=0.2, random_state=42)
    _, _, y_train_somme, y_test_somme = train_test_split(X, y_somme, test_size=0.2, random_state=42)

    # Initialiser les modèles
    model_rf_transactions = RandomForestRegressor(n_estimators=100, random_state=42)
    model_xgb_transactions = XGBRegressor()
    model_lr_transactions = LinearRegression()
    model_svm_transactions = SVR(kernel='linear', C=1.0)
    model_rf_somme = RandomForestRegressor(n_estimators=100, random_state=42)
    model_xgb_somme = XGBRegressor()
    model_lr_somme = LinearRegression()
    model_svm_somme = SVR(kernel='linear', C=1.0)

    # Entraîner les modèles sur les données d'entraînement
    model_rf_transactions.fit(X_train, y_train_transactions)
    model_xgb_transactions.fit(X_train, y_train_transactions)
    model_lr_transactions.fit(X_train, y_train_transactions)
    model_svm_transactions.fit(X_train, y_train_transactions)
    model_rf_somme.fit(X_train, y_train_somme)
    model_xgb_somme.fit(X_train, y_train_somme)
    model_lr_somme.fit(X_train, y_train_somme)
    model_svm_somme.fit(X_train, y_train_somme)

    # Faire des prédictions sur les données de test
    y_pred_rf_transactions = model_rf_transactions.predict(X_test)
    y_pred_xgb_transactions = model_xgb_transactions.predict(X_test)
    y_pred_lr_transactions = model_lr_transactions.predict(X_test)
    y_pred_svm_transactions = model_svm_transactions.predict(X_test)
    y_pred_rf_somme = model_rf_somme.predict(X_test)
    y_pred_xgb_somme = model_xgb_somme.predict(X_test)
    y_pred_lr_somme = model_lr_somme.predict(X_test)
    y_pred_svm_somme = model_svm_somme.predict(X_test)

    # Calculer les coefficients de détermination R2 pour chaque modèle
    r2_rf_transactions = r2_score(y_test_transactions, y_pred_rf_transactions)
    r2_xgb_transactions = r2_score(y_test_transactions, y_pred_xgb_transactions)
    r2_lr_transactions = r2_score(y_test_transactions, y_pred_lr_transactions)
    r2_svm_transactions = r2_score(y_test_transactions, y_pred_svm_transactions)
    r2_rf_somme = r2_score(y_test_somme, y_pred_rf_somme)
    r2_xgb_somme = r2_score(y_test_somme, y_pred_xgb_somme)
    r2_lr_somme = r2_score(y_test_somme, y_pred_lr_somme)
    r2_svm_somme = r2_score(y_test_somme, y_pred_svm_somme)

    # Créer un graphique
    models = ['RandomForest', 'XGBoost', 'LinearRegression', 'SVM']
    r2_transactions = [r2_rf_transactions, r2_xgb_transactions, r2_lr_transactions, r2_svm_transactions]
    r2_somme = [r2_rf_somme, r2_xgb_somme, r2_lr_somme, r2_svm_somme]

    fig, ax = plt.subplots(1, 2, figsize=(12, 6))

    ax[0].barh(models, r2_transactions, color='lightblue')
    ax[0].set_xlabel('R2')
    ax[0].set_title('R2 pour la prédiction du nombre de transactions')

    ax[1].barh(models, r2_somme, color='salmon')
    ax[1].set_xlabel('R2')
    ax[1].set_title('R2 pour la prédiction de la somme')

    st.pyplot(fig)

if __name__ == "__main__":
    main()
