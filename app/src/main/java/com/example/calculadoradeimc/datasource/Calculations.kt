/*

NO-LONGER USED

* Cérebro do aplicativo.
* Object (singleton): ferramenta estática chamada sem criar instância.
* Função calculateimc: entra height e weight como string;
*   troca , por .; transforma em numero ou null; calcula
*   callback/response: retorna mensagem e erro para UI

package com.example.calculadoradeimc.datasource

import android.annotation.SuppressLint

object Calculations {

    @SuppressLint("DefaultLocale")
    fun calculateIMC(height: String, weight: String, response: (String, Boolean) -> Unit){

        if(height.isNotEmpty() && weight.isNotEmpty()){

            val weightFormat = weight.replace(",",".").toDoubleOrNull()
            val heightFormat = height.toDoubleOrNull()

            if(weightFormat != null && heightFormat != null){
                val imc = weightFormat / (heightFormat / 100 * heightFormat / 100)
                val imcFormatted = String.format("%.2f",imc)

                when{
                    imc < 18.5 -> response("IMC: $imcFormatted \n Abaixo do peso",false)
                    imc in 18.5 .. 24.9 -> response("IMC : $imcFormatted \n Peso normal", false)
                    imc in 25.0 .. 29.9 -> response("IMC : $imcFormatted \n Sobrepeso", false)
                    imc in 30.0 .. 34.9 -> response("IMC : $imcFormatted \n Obesidade (Grau 1)", false)
                    imc in 35.0 .. 39.9 -> response("IMC : $imcFormatted \n Obesidade severa(Grau 2)", false)
                    else -> response("IMC: $imcFormatted \n Obesidade Mórbida (Grau 3)", false)
                }
            }

        }else{
            response("Preencha todos os campos!",true)
        }

    }

}

 */