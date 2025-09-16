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
    println("== DEMOSTRACION - Cuentas ==")

    // Ejemplo 1: Cuenta de Ahorros
    val ahorro = CuentaAhorros(saldoInicial = 12000f, tasaAnual = 5f)
    ahorro.imprimir()
    println("--- Operaciones en Cuenta de Ahorros ---")
    ahorro.retirar(2000f)    // retiro permitido (activa)
    ahorro.consignar(500f)   // consignación permitida
    ahorro.retirar(1000f)
    ahorro.retirar(1000f)
    ahorro.retirar(1000f)
    ahorro.retirar(1000f)    // ahora tendrá >4 retiros -> comisión extra en extracto
    ahorro.imprimir()
    ahorro.extractoMensual()
    ahorro.imprimir()

    println("\n--------------------------------\n")

    // Ejemplo 2: Cuenta Corriente con sobregiro
    val corriente = CuentaCorriente(saldoInicial = 500f, tasaAnual = 3f)
    corriente.imprimir()
    println("--- Operaciones en Cuenta Corriente ---")
    corriente.retirar(800f)  // genera sobregiro de 300
    corriente.imprimir()
    corriente.consignar(200f) // reduce sobregiro a 100
    corriente.imprimir()
    corriente.consignar(150f) // sobra 50 -> saldo 50
    corriente.imprimir()
    corriente.extractoMensual()
    corriente.imprimir()
}
