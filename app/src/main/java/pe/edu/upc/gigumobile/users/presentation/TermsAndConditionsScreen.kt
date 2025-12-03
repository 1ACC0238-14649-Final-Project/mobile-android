package pe.edu.upc.gigumobile.users.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TermsAndConditionsScreen(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    isReadOnly: Boolean = false
) {
    var accepted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Título y contenido
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Términos y Condiciones",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Última actualización: Diciembre 2024",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Contenido de términos
            TermsContent()

            // Checkbox de aceptación (solo si no es solo lectura)
            if (!isReadOnly) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = accepted,
                        onCheckedChange = { accepted = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "He leído y acepto los términos y condiciones",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Botones
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isReadOnly) {
                // Si es solo lectura (desde perfil), solo mostrar botón de cerrar
                Button(
                    onClick = onAccept, // Usa onAccept para cerrar
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar")
                }
            } else {
                // Si es desde login, mostrar botones de aceptar/rechazar
                Button(
                    onClick = onAccept,
                    enabled = accepted,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aceptar y continuar")
                }

                TextButton(
                    onClick = onDecline,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Rechazar")
                }
            }
        }
    }
}

@Composable
fun TermsContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TermsSection(
            title = "1. Aceptación de los Términos",
            content = "Al acceder y utilizar Gigu, aceptas cumplir con estos términos y condiciones. Gigu es una plataforma que conecta compradores (buyers) con vendedores (sellers) de servicios digitales. Si no estás de acuerdo con estos términos, no debes utilizar la plataforma."
        )

        TermsSection(
            title = "2. Tipos de Usuario",
            content = "Gigu permite dos tipos de cuentas: Compradores (Buyers) que contratan servicios, y Vendedores (Sellers) que ofrecen servicios a través de Gigs. Puedes tener una cuenta de cada tipo, pero cada cuenta debe cumplir con las responsabilidades correspondientes."
        )

        TermsSection(
            title = "3. Gigs y Servicios",
            content = "Los Vendedores son responsables de la precisión, calidad y legalidad de los Gigs que publican. Los Gigs deben describir claramente el servicio ofrecido, precio, tiempo de entrega y cualquier requisito. Está prohibido publicar contenido ilegal, engañoso o que viole derechos de terceros."
        )

        TermsSection(
            title = "4. Pulls y Transacciones",
            content = "Al crear un Pull, el Comprador acepta contratar el servicio al precio acordado. Los precios pueden negociarse entre las partes, pero una vez aceptado un Pull, ambas partes deben cumplir con los términos acordados. Gigu actúa como intermediario pero no garantiza la calidad del servicio."
        )

        TermsSection(
            title = "5. Pagos y Reembolsos",
            content = "Los pagos se procesan según el estado del Pull (pending, in_process, payed, complete). Los Compradores deben pagar según lo acordado. Las políticas de reembolso se rigen por el estado del Pull y las políticas de Gigu. Los reembolsos deben ser solicitados a través de la plataforma."
        )

        TermsSection(
            title = "6. Comunicación entre Usuarios",
            content = "La comunicación entre Compradores y Vendedores debe realizarse a través de la plataforma. Está prohibido compartir información de contacto personal antes de completar una transacción. Gigu puede monitorear las comunicaciones para garantizar el cumplimiento de estos términos."
        )

        TermsSection(
            title = "7. Responsabilidades del Vendedor",
            content = "Los Vendedores deben entregar servicios de calidad según lo descrito en sus Gigs, cumplir con los plazos acordados, y mantener comunicación profesional con los Compradores. El incumplimiento puede resultar en la suspensión o cancelación de la cuenta."
        )

        TermsSection(
            title = "8. Responsabilidades del Comprador",
            content = "Los Compradores deben proporcionar información clara y precisa sobre sus necesidades, respetar los términos acordados, y realizar los pagos según lo establecido. El abuso del sistema de reembolsos o comportamiento inapropiado puede resultar en la suspensión de la cuenta."
        )

        TermsSection(
            title = "9. Cancelaciones y Disputas",
            content = "Las cancelaciones deben ser acordadas por ambas partes o gestionadas a través del sistema de disputas de Gigu. En caso de disputa, Gigu puede intervenir como mediador, pero la resolución final depende de las partes involucradas y las políticas de la plataforma."
        )

        TermsSection(
            title = "10. Cuenta y Seguridad",
            content = "Eres responsable de mantener la confidencialidad de tu cuenta y contraseña. Debes notificarnos inmediatamente sobre cualquier uso no autorizado. No puedes compartir tu cuenta con terceros ni crear múltiples cuentas para evadir restricciones."
        )

        TermsSection(
            title = "11. Propiedad Intelectual",
            content = "El contenido de los Gigs y los servicios entregados son propiedad de sus respectivos creadores. Al publicar un Gig, otorgas a Gigu una licencia para mostrar y promocionar tu contenido en la plataforma. Los Compradores adquieren los derechos según lo acordado en cada transacción."
        )

        TermsSection(
            title = "12. Privacidad y Datos",
            content = "Tu privacidad es importante. Al usar Gigu, aceptas nuestra política de privacidad y el procesamiento de tus datos personales. Compartimos información necesaria entre Compradores y Vendedores para completar transacciones, pero protegemos tu información personal según nuestras políticas."
        )

        TermsSection(
            title = "13. Limitación de Responsabilidad",
            content = "Gigu actúa como plataforma intermediaria. No somos responsables por la calidad, legalidad o cumplimiento de los servicios ofrecidos por los Vendedores. Los usuarios interactúan entre sí bajo su propio riesgo. Gigu no garantiza resultados específicos de ningún servicio."
        )

        TermsSection(
            title = "14. Modificaciones de los Términos",
            content = "Nos reservamos el derecho de modificar estos términos en cualquier momento. Los cambios entrarán en vigor después de su publicación. El uso continuado de la plataforma después de los cambios constituye tu aceptación de los nuevos términos."
        )

        TermsSection(
            title = "15. Suspensión y Terminación",
            content = "Gigu se reserva el derecho de suspender o terminar cuentas que violen estos términos, muestren comportamiento fraudulento, o dañen la integridad de la plataforma. Las decisiones de suspensión son finales y no apelables."
        )

        TermsSection(
            title = "16. Contacto y Soporte",
            content = "Para preguntas sobre estos términos, disputas, o soporte técnico, puedes contactarnos a través de la aplicación. Nos comprometemos a responder en un plazo razonable y ayudar a resolver cualquier problema relacionado con el uso de la plataforma."
        )
    }
}

@Composable
fun TermsSection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp
        )
    }
}

