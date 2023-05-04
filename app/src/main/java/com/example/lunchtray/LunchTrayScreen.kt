/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.lunchtray

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lunchtray.datasource.DataSource
import com.example.lunchtray.ui.AccompanimentMenuScreen
import com.example.lunchtray.ui.CheckoutScreen
import com.example.lunchtray.ui.EntreeMenuScreen
import com.example.lunchtray.ui.OrderViewModel
import com.example.lunchtray.ui.SideDishMenuScreen
import com.example.lunchtray.ui.StartOrderScreen

enum class LunchTrayScreen(@StringRes val title: Int) {
    Inicio(title = R.string.app_name),
    MenuPlatosPrincipales(title = R.string.choose_entree),
    MenuGuarniciones(title = R.string.choose_side_dish),
    MenuAcompaniamientos(title = R.string.choose_accompaniment),
    ConfirmacionCompra(title = R.string.order_checkout)
}

@Composable
fun LunchTrayAppBar(
    @StringRes actualScreen: Int,
    flag: Boolean,
    icono: () -> Unit,
    modifier: Modifier = Modifier) {
    TopAppBar(
        title = {
            Text(stringResource(actualScreen))
        },
        modifier = modifier,
        navigationIcon = {
            if (flag) {
                IconButton(onClick = icono) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button))
                }
            }
        })
}

@Composable
fun LunchTrayApp(modifier: Modifier = Modifier) {
    val controlador = rememberNavController()
    val backStackEntry by controlador.currentBackStackEntryAsState()
    val pantallaactual = LunchTrayScreen.valueOf(
        backStackEntry?.destination?.route ?: LunchTrayScreen.Inicio.name)
    val viewModel: OrderViewModel = viewModel()

    Scaffold(
        topBar = {
            LunchTrayAppBar(
                actualScreen = pantallaactual.title,
                flag = controlador.previousBackStackEntry != null,
                icono = { controlador.navigateUp() })
        }) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        NavHost(
            navController = controlador,
            startDestination = LunchTrayScreen.Inicio.name,
            modifier = modifier.padding(innerPadding),) {
            composable(route = LunchTrayScreen.Inicio.name) {
                StartOrderScreen(
                    onStartOrderButtonClicked = {
                        controlador.navigate(LunchTrayScreen.MenuPlatosPrincipales.name)
                    })
            }
            composable(route = LunchTrayScreen.MenuPlatosPrincipales.name) {
                EntreeMenuScreen(
                    options = DataSource.entreeMenuItems,
                    onCancelButtonClicked = {
                        viewModel.resetOrder()
                        controlador.popBackStack(LunchTrayScreen.Inicio.name, inclusive = false)
                    },
                    onNextButtonClicked = {
                        controlador.navigate(LunchTrayScreen.MenuGuarniciones.name)
                    },
                    onSelectionChanged = { item ->
                        viewModel.updateEntree(item)
                    })
            }
            composable(route = LunchTrayScreen.MenuGuarniciones.name) {
                SideDishMenuScreen(
                    options = DataSource.sideDishMenuItems,
                    onCancelButtonClicked = {
                        viewModel.resetOrder()
                        controlador.popBackStack(LunchTrayScreen.Inicio.name, inclusive = false)
                    },
                    onNextButtonClicked = {
                        controlador.navigate(LunchTrayScreen.MenuAcompaniamientos.name)
                    },
                    onSelectionChanged = { item ->
                        viewModel.updateSideDish(item)
                    })
            }
            composable(route = LunchTrayScreen.MenuAcompaniamientos.name) {
                AccompanimentMenuScreen(
                    options = DataSource.accompanimentMenuItems,
                    onCancelButtonClicked = {
                        viewModel.resetOrder()
                        controlador.popBackStack(LunchTrayScreen.Inicio.name, inclusive = false)
                    },
                    onNextButtonClicked = {
                        controlador.navigate(LunchTrayScreen.ConfirmacionCompra.name)
                    },
                    onSelectionChanged = { item ->
                        viewModel.updateAccompaniment(item)
                    })
            }
            composable(route = LunchTrayScreen.ConfirmacionCompra.name) {
                CheckoutScreen(
                    orderUiState = uiState,
                    onCancelButtonClicked = {
                        viewModel.resetOrder()
                        controlador.popBackStack(LunchTrayScreen.Inicio.name, inclusive = false)
                    },
                    onNextButtonClicked = {
                        viewModel.resetOrder()
                        controlador.popBackStack(LunchTrayScreen.Inicio.name, inclusive = false)
                    })
            }
        }
    }
}
