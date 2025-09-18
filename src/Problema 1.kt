open class Cuenta(
    /*Saldo de la cuenta */
    protected var saldo: Float,
    protected var tasaAnual: Float ) {
    protected var numeroConsignaciones: Int = 0
    protected var numeroRetiros: Int = 0
    protected var comisionMensual: Float = 0f


    open fun consignar(cantidad: Float) {
        if (cantidad <= 0f) {
            println("Intento de consignar una cantidad no válida: $cantidad")
            return
        }
        saldo += cantidad
        numeroConsignaciones++
    }

    /**
     * Retira una cantidad de la cuenta si hay saldo suficiente. No permite
     * que el saldo quede negativo en la clase deñ principio
     */

    open fun retirar(cantidad: Float) {
        if (cantidad <= 0f) {
            println("Intento de retirar una cantidad no válida: $cantidad")
            return
        }
        if (cantidad > saldo) {
            println("Retiro rechazado: la cantidad $cantidad supera el saldo disponible $saldo")
            return
        }
        saldo -= cantidad
        numeroRetiros++
    }

    protected open fun calcularInteresMensual() {
        val interes = saldo * (tasaAnual / 100f) / 12f
        saldo += interes
    }

    open fun extractoMensual() {
        saldo -= comisionMensual
        calcularInteresMensual()
        // Reiniciar contadores y comisión para el próximo periodo
        numeroConsignaciones = 0
        numeroRetiros = 0
        comisionMensual = 0f
    }

    open fun imprimir() {
        println("=== Cuenta ===")
        println("Saldo: $saldo")
        println("Comisión mensual: $comisionMensual")
        println("Número consignaciones: $numeroConsignaciones")
        println("Número retiros: $numeroRetiros")
    }
}

/**
 ----- Cuenta de ahorros -----
 La cuenta tiene un estado 'activa' cuando el saldo >= 10000. Si está inactiva,
 no se permiten consignaciones ni retiros.
 */

class CuentaAhorros(
    saldoInicial: Float,
    tasaAnual: Float
) : Cuenta(saldoInicial, tasaAnual) {

    val activa: Boolean
        get() = saldo >= 10000f

    override fun consignar(cantidad: Float) {
        if (!activa) {
            println("La cuenta de ahorros está inactiva (saldo = $saldo). No se puede consignar.")
            return
        }
        super.consignar(cantidad)
    }

    override fun retirar(cantidad: Float) {
        if (!activa) {
            println("La cuenta de ahorros está inactiva (saldo = $saldo). No se puede retirar.")
            return
        }
        super.retirar(cantidad)
    }

    override fun extractoMensual() {
        // Si hay más de 4 retiros, cada retiro adicional genera una comisión de 1000
        if (numeroRetiros > 4) {
            val retirosExtra = numeroRetiros - 4
            val montoExtra = retirosExtra * 1000f
            comisionMensual += montoExtra
        }
        // Llamar al comportamiento genérico (aplica comisión, calcula interés y reinicia contadores)
        super.extractoMensual()
        // Después del extracto podemos informar si quedó activa o no (opcional)
        println("Extracto mensual (Cuenta de Ahorros) generado. Cuenta activa: $activa")
    }

    override fun imprimir() {
        val transacciones = numeroConsignaciones + numeroRetiros
        println("=== Cuenta de Ahorros ===")
        println("Saldo: $saldo")
        println("Comisión mensual: $comisionMensual")
        println("Número de transacciones (consignaciones + retiros): $transacciones")
        println("Estado: ${if (activa) "Activa" else "Inactiva"}")
    }
}

class CuentaCorriente(
    saldoInicial: Float,
    tasaAnual: Float
) : Cuenta(saldoInicial, tasaAnual) {


    private var sobregiro: Float = 0f

    override fun retirar(cantidad: Float) {
        if (cantidad <= 0f) {
            println("Intento de retirar una cantidad no válida: $cantidad")
            return
        }

        // Si hay suficiente saldo, comportamiento normal
        if (cantidad <= saldo) {
            saldo -= cantidad
        } else {
            // Se consume todo el saldo y la diferencia queda como sobregiro
            val excedente = cantidad - saldo
            saldo = 0f
            sobregiro += excedente
        }
        numeroRetiros++
    }

    override fun consignar(cantidad: Float) {
        if (cantidad <= 0f) {
            println("Intento de consignar una cantidad no válida: $cantidad")
            return
        }

        // Si existe sobregiro, la consignación primero lo reduce
        if (sobregiro > 0f) {
            if (cantidad >= sobregiro) {
                val sobrante = cantidad - sobregiro
                sobregiro = 0f
                saldo += sobrante
            } else {
                sobregiro -= cantidad
            }
        } else {
            saldo += cantidad
        }
        numeroConsignaciones++
    }

    override fun extractoMensual() {
        super.extractoMensual()
    }

    override fun imprimir() {
        val transacciones = numeroConsignaciones + numeroRetiros
        println("=== Cuenta Corriente ===")
        println("Saldo: $saldo")
        println("Comisión mensual: $comisionMensual")
        println("Número de transacciones (consignaciones + retiros): $transacciones")
        println("Sobregiro: $sobregiro")
    }
}


fun main() {
    println("=== Simulación de Cuentas Bancarias ===")

    // Elegir tipo de cuenta
    println("Seleccione el tipo de cuenta:")
    println("1. Cuenta de Ahorros")
    println("2. Cuenta Corriente")
    print("Opción: ")
    val tipoCuenta = readLine()?.toIntOrNull()

    print("Ingrese saldo inicial: ")
    val saldo = readLine()?.toFloatOrNull() ?: 0f

    print("Ingrese tasa anual (%): ")
    val tasa = readLine()?.toFloatOrNull() ?: 0f

    val cuenta: Cuenta = when (tipoCuenta) {
        1 -> CuentaAhorros(saldo, tasa)
        2 -> CuentaCorriente(saldo, tasa)
        else -> {
            println("⚠️ Opción inválida. Se creará Cuenta de Ahorros por defecto.")
            CuentaAhorros(saldo, tasa)
        }
    }

    println("\n✅ Cuenta creada con éxito")
    cuenta.imprimir()

    // Menú interactivo
    while (true) {
        println(
            """
            
            --- MENÚ ---
            1. Consignar dinero
            2. Retirar dinero
            3. Generar extracto mensual
            4. Mostrar estado de la cuenta
            5. Salir
            """.trimIndent()
        )

        print("Elija una opción: ")
        when (readLine()?.toIntOrNull()) {
            1 -> {
                print("Ingrese cantidad a consignar: ")
                val cantidad = readLine()?.toFloatOrNull() ?: 0f
                cuenta.consignar(cantidad)
            }
            2 -> {
                print("Ingrese cantidad a retirar: ")
                val cantidad = readLine()?.toFloatOrNull() ?: 0f
                cuenta.retirar(cantidad)
            }
            3 -> {
                println("Generando extracto mensual...")
                cuenta.extractoMensual()
            }
            4 -> cuenta.imprimir()
            5 -> {
                println("👋 Saliendo del programa. ¡Hasta pronto!")
                return
            }
            else -> println("⚠️ Opción no válida, intente de nuevo.")
        }
    }
}

