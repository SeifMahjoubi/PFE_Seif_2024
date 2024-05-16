import streamlit as st
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from xgboost import XGBRegressor

# Fonction pour calculer la performance après les prédictions
def calculer_perf_apres_prediction(data_point_vente, predictions_transactions, predictions_somme, max_transactions, max_somme):
    data_point_vente['K1'] = predictions_transactions / max_transactions
    data_point_vente['K2'] = predictions_somme / max_somme
    data_point_vente['perf_predite'] = (np.exp(data_point_vente['K1']) * np.exp(data_point_vente['K2'])) / np.exp(2)
    limites_intervals = [0, 0.2, 0.4, 0.6, 0.8, 1]
    etiquettes = [1, 2, 3, 4, 5]
    data_point_vente['perf_predite'] = pd.cut(data_point_vente['perf_predite'], bins=limites_intervals, labels=etiquettes)
    return data_point_vente

def main():
    # Titre de l'application
    st.title("Prédictions des ventes pour les 12 mois suivants")

    # Charger les données
    data = pd.read_csv("C:/Users/the cast/OneDrive/Bureau/POS_DATA/Analyse Prédictive et Classification .csv")

    # Filtrer les données pour ne garder que les lignes avec des valeurs valides de Somme_transactions
    data = data.dropna(subset=['Somme_transactions'])

    # Sélectionner les régions uniques
    regions_uniques = data['Region'].unique()

    # Sélectionner la région
    region_selectionnee = st.selectbox("Sélectionner la région", regions_uniques)

    # Filtrer les données pour ne garder que les lignes correspondant à la région sélectionnée
    data_region = data[data['Region'] == region_selectionnee]

    # Sélectionner les wilayas uniques dans la région sélectionnée
    wilayas_uniques = data_region['Wilaya'].unique()

    # Sélectionner la wilaya
    wilaya_selectionnee = st.selectbox("Sélectionner la wilaya", wilayas_uniques)

    # Filtrer les données pour ne garder que les lignes correspondant à la wilaya sélectionnée
    data_wilaya = data_region[data_region['Wilaya'] == wilaya_selectionnee]

    # Sélectionner les communes uniques dans la wilaya sélectionnée
    communes_uniques = data_wilaya['Commune'].unique()

    # Sélectionner la commune
    commune_selectionnee = st.selectbox("Sélectionner la commune", communes_uniques)

    # Filtrer les données pour ne garder que les lignes correspondant à la commune sélectionnée
    data_commune = data_wilaya[data_wilaya['Commune'] == commune_selectionnee]

    # Sélectionner les points de vente uniques dans la commune sélectionnée
    points_vente_uniques = data_commune['Code_PDV'].unique()

    # Initialiser les dictionnaires pour stocker les prédictions pour chaque point de vente
    predictions_transactions_pdv = {}
    predictions_somme_pdv = {}

    # Entraîner les modèles pour chaque point de vente et obtenir les prédictions
    for point_vente in points_vente_uniques:
        data_pdv = data_commune[data_commune['Code_PDV'] == point_vente]
        
        X_pdv = data_pdv[['Mois']].values
        y_transactions_pdv = data_pdv['Nombre_transactions'].values
        y_somme_pdv = data_pdv['Somme_transactions'].values

        # Entraîner le modèle XGBoost pour les transactions
        model_xgb_transactions_pdv = XGBRegressor()
        model_xgb_transactions_pdv.fit(X_pdv, y_transactions_pdv)
        transactions_predictions_pdv = model_xgb_transactions_pdv.predict(X_pdv)

        # Entraîner le modèle XGBoost pour la somme des transactions
        model_xgb_somme_pdv = XGBRegressor()
        model_xgb_somme_pdv.fit(X_pdv, y_somme_pdv)
        somme_predictions_pdv = model_xgb_somme_pdv.predict(X_pdv)

        # Stocker les prédictions dans les dictionnaires
        predictions_transactions_pdv[point_vente] = transactions_predictions_pdv
        predictions_somme_pdv[point_vente] = somme_predictions_pdv

    # Obtenir les valeurs maximales des prédictions pour la commune
    max_transactions = max([predictions.max() for predictions in predictions_transactions_pdv.values()])
    max_somme = max([predictions.max() for predictions in predictions_somme_pdv.values()])

    # Sélectionner le point de vente
    point_vente_selectionne = st.selectbox("Sélectionner le point de vente", points_vente_uniques)

    # Filtrer les données pour ne garder que les lignes correspondant au point de vente sélectionné
    data_point_vente = data_commune[data_commune['Code_PDV'] == point_vente_selectionne]

    # Sauvegarder les données réelles 
    transactions_reelles = data_point_vente['Nombre_transactions'].tail(12).values
    sommes_reelles = data_point_vente['Somme_transactions'].tail(12).values

    # Préparer les données pour la régression linéaire
    X = data_point_vente[['Mois']].values
    y_transactions = data_point_vente['Nombre_transactions'].values
    y_somme = data_point_vente['Somme_transactions'].values

    # Entraîner le modèle XGBoost pour les transactions
    model_xgb_transactions = XGBRegressor()
    model_xgb_transactions.fit(X, y_transactions)
    transactions_prevues_xgb = model_xgb_transactions.predict(X[-12:])

    # Entraîner le modèle XGBoost pour la somme des transactions (revenus )
    model_xgb_somme = XGBRegressor()
    model_xgb_somme.fit(X, y_somme)
    somme_prevue_xgb = model_xgb_somme.predict(X[-12:])

    # Calculer la performance après les prédictions
    data_point_vente = calculer_perf_apres_prediction(data_point_vente, transactions_prevues_xgb, somme_prevue_xgb, max_transactions, max_somme)

    # Performance actuelle et prédite
    perfs_actuelles = data_point_vente['perf'].tail(12).values
    perfs_predites = data_point_vente['perf_predite'].tail(12).values

    # Afficher le titre des graphiques prédictifs
    st.header("Prédictions des ventes")

    fig, axes = plt.subplots(nrows=2, ncols=2, figsize=(12, 10), gridspec_kw={'hspace': 0.5, 'wspace': 0.5})

    # Graphique des prédictions de nombre de transactions
    axes[0, 0].plot(data_point_vente['Mois'].tail(12), transactions_prevues_xgb, label='Transactions prévues', marker='x')
    axes[0, 0].set_xlabel('Mois')
    axes[0, 0].set_ylabel('Nombre de transactions')
    axes[0, 0].legend()
    axes[0, 0].set_title('Transactions prévues')

    # Graphique des prédictions de somme des transactions
    axes[0, 1].plot(data_point_vente['Mois'].tail(12), somme_prevue_xgb, label='Sommes prévues', marker='x')
    axes[0, 1].set_xlabel('Mois')
    axes[0, 1].set_ylabel('Somme des transactions')
    axes[0, 1].legend()
    axes[0, 1].set_title('Sommes  prévues')

    # Graphique de la performance actuelle et prédite
    axes[1, 0].plot(data_point_vente['Mois'].tail(12), perfs_actuelles, label='Performance actuelle', marker='o')
    axes[1, 0].plot(data_point_vente['Mois'].tail(12), perfs_predites, label='Performance prédite', marker='x')
    axes[1, 0].set_xlabel('Mois')
    axes[1, 0].set_ylabel('Performance')
    axes[1, 0].legend()
    axes[1, 0].set_title('Performance actuelle et prédite')

    # Graphique en secteurs ou camembert
    pourcentages = data_point_vente['perf_predite'].value_counts(normalize=True) * 100
    labels = pourcentages.index.astype(str)
    axes[1, 1].pie(pourcentages, labels=labels, autopct='%1.1f%%')
    axes[1, 1].set_title('Répartition des performances prédites')

    # Afficher les graphiques
    st.pyplot(fig)

    # Ajouter un espace
    st.write('')

if __name__ == "__main__":
    main()
