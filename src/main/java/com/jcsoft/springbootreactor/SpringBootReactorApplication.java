package com.jcsoft.springbootreactor;

import com.jcsoft.springbootreactor.model.Comentarios;
import com.jcsoft.springbootreactor.model.Usuario;
import com.jcsoft.springbootreactor.model.UsuarioComentario;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class SpringBootReactorApplication
        implements CommandLineRunner
{
    private static final Logger LOG = LoggerFactory.getLogger(SpringBootReactorApplication.class);

    public static void main(String[] args)
    {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args)
            throws Exception
    {
        LOG.info("Ejemplo Iterable");
        ejemploIterable();
        LOG.info("Ejemplo FlatMap");
        ejemploFlatMap();
        LOG.info("Ejemplo ToString");
        ejemploToString();
        LOG.info("Ejemplo CollectList");
        ejemploCollectList();
        LOG.info("Ejemplo UsuarioComentario FlatMap");
        ejemploUsuarioComentariosFlatMap();
        LOG.info("Ejemplo UsuarioComentario ZipWith");
        ejemploUsuarioComentariosZipWith();
        LOG.info("Ejemplo UsuarioComentario ZipWith 2");
        ejemploUsuarioComentariosZipWith2();
        LOG.info("Ejemplo Zip With with Range");
        ejemploZipWithRange();
        LOG.info("Ejemplo Time Interval");
        //ejemploInterval();
        LOG.info("Ejemplo Time Delay");
        //ejemploDelayElements();
        LOG.info("Ejemplo IntervalInfinito");
        //ejemploIntervalInfinito();
        LOG.info("Ejemplo IntervalInfinitoDeCreate");
        //ejemploIntervalInfinitoDeCreate();
        LOG.info("Ejemplo Contrapresion");
        ejemploContraPresion();
    }

    private void ejemploIterable()
    {
        List<String> usuariosLst = new ArrayList<>();
        usuariosLst.add("Jorge Cueva");
        usuariosLst.add("Lucia Castilla");
        usuariosLst.add("Jose Díaz");
        usuariosLst.add("Juan Salazar");
        usuariosLst.add("Daniel Meneses");
        usuariosLst.add("Bruce Lee");
        usuariosLst.add("Bruce Willis");
        // nombres es un observable
        Flux<String> nombres = Flux.fromIterable(usuariosLst);
        //Flux.just("Jorge Cueva", "Lucia Castilla", "Jose Díaz", "Juan Salazar", "Daniel Meneses", "Bruce Lee", "Bruce Willis");

        // aqui al aplicar todos los operadores a nombres no modifica el flujo principal, sino que se crea uno nuevo (usuarios)
        Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0],
                                                                   nombre.split(" ")[1]))
                                        .filter(usuario -> !usuario.getNombre().equalsIgnoreCase("bruce"))
                                        .doOnNext(usuario -> {
                                            if (usuario == null || usuario.getNombre().isEmpty()) {
                                                throw new RuntimeException("Nombres no pueden ser vacios");
                                            }
                                            System.out.println(usuario.getNombre());
                                        })
                                        //.map(String::toUpperCase)
                                        .map(usuario -> { // esto se reflejara en el subscribe, en el doOnNext de arriba no
                                            usuario.setNombre(usuario.getNombre().toLowerCase());
                                            return usuario;
                                        });
        // aqui generamos un observador que va a consumir lo producido
        // ambos se ejecutan en el onNext
        usuarios.subscribe(usuario -> LOG.info(usuario.toString()),
                           error -> LOG.error(error.getMessage()),
                           () -> LOG.info(
                                   "Ha finalizado la ejecucion del flujo")); //Runnable es una functional interface
    }

    private void ejemploFlatMap()
    {
        List<String> usuariosLst = new ArrayList<>();
        usuariosLst.add("Jorge Cueva");
        usuariosLst.add("Lucia Castilla");
        usuariosLst.add("Jose Díaz");
        usuariosLst.add("Juan Salazar");
        usuariosLst.add("Daniel Meneses");
        usuariosLst.add("Bruce Lee");
        usuariosLst.add("Bruce Willis");

        Flux.fromIterable(usuariosLst)
            .map(nombre -> new Usuario(nombre.split(" ")[0],
                                       nombre.split(" ")[1]))
            .flatMap(usuario -> {
                if (usuario.getNombre().equalsIgnoreCase("Bruce")) {
                    return Mono.just(usuario);
                } else {
                    return Mono.empty();
                }
            })
            .map(usuario -> {
                usuario.setNombre(usuario.getNombre().toLowerCase());
                return usuario;
            })
            .subscribe(usuario -> LOG.info(usuario.toString())); //Runnable es una functional interface
    }

    private void ejemploToString()
    {
        List<Usuario> usuariosLst = new ArrayList<>();
        usuariosLst.add(new Usuario("Jorge", "Cueva"));
        usuariosLst.add(new Usuario("Lucia", "Castilla"));
        usuariosLst.add(new Usuario("Jose", "Díaz"));
        usuariosLst.add(new Usuario("Juan", "Salazar"));
        usuariosLst.add(new Usuario("Daniel", "Meneses"));
        usuariosLst.add(new Usuario("Bruce", "Lee"));
        usuariosLst.add(new Usuario("Bruce", "Willis"));

        Flux.fromIterable(usuariosLst)
            .map(usuario -> usuario.getNombre().toUpperCase() + " " + usuario.getApellido().toUpperCase())
            .flatMap(nombre -> {
                if (nombre.startsWith("Bruce".toUpperCase())) {
                    return Mono.just(nombre);
                } else {
                    return Mono.empty();
                }
            })
            .map(nombre -> {
                return nombre.toLowerCase();
            })
            .subscribe(usuario -> LOG.info(usuario.toString())); //Runnable es una functional interface
    }

    private void ejemploCollectList()
    {
        List<Usuario> usuariosLst = new ArrayList<>();
        usuariosLst.add(new Usuario("Jorge", "Cueva"));
        usuariosLst.add(new Usuario("Lucia", "Castilla"));
        usuariosLst.add(new Usuario("Jose", "Díaz"));
        usuariosLst.add(new Usuario("Juan", "Salazar"));
        usuariosLst.add(new Usuario("Daniel", "Meneses"));
        usuariosLst.add(new Usuario("Bruce", "Lee"));
        usuariosLst.add(new Usuario("Bruce", "Willis"));

        Flux.fromIterable(usuariosLst)
            .collectList()
            .subscribe(lista -> {
                lista.forEach(item -> LOG.info(item.toString()));
            }); //Runnable es una functional interface
    }

    private void ejemploUsuarioComentariosFlatMap()
    {
        Mono<Usuario> usuarioMono =
                Mono.fromCallable(() -> Usuario.builder().nombre("Jorge").apellido("Cueva").build());

        Mono<Comentarios> comentariosMono = Mono.fromCallable(
                () -> Comentarios.builder()
                                 .comentario("Hola Jorge que tal")
                                 .comentario("Mañana tenemos reunion a primera hora")
                                 .comentario("Yo tambien estoy llevando el curso de Spring")
                                 .build());

        usuarioMono.flatMap(
                u -> comentariosMono.map(cm -> UsuarioComentario.builder().usuario(u).comentarios(cm).build()))
                   .subscribe(uc -> LOG.info(uc.toString()));
    }

    private void ejemploUsuarioComentariosZipWith()
    {
        Mono<Usuario> usuarioMono =
                Mono.fromCallable(() -> Usuario.builder().nombre("Jorge").apellido("Cueva").build());

        Mono<Comentarios> comentariosMono = Mono.fromCallable(
                () -> Comentarios.builder()
                                 .comentario("Hola Jorge que tal")
                                 .comentario("Mañana tenemos reunion a primera hora")
                                 .comentario("Yo tambien estoy llevando el curso de Spring")
                                 .build());

        Mono<UsuarioComentario> usuarioConComentarios =
                usuarioMono.zipWith(comentariosMono, (usuario, comentarios) -> UsuarioComentario.builder()
                                                                                                .usuario(usuario)
                                                                                                .comentarios(
                                                                                                        comentarios)
                                                                                                .build());
        usuarioConComentarios.subscribe(uc -> LOG.info(uc.toString()));
    }

    private void ejemploUsuarioComentariosZipWith2()
    {
        Mono<Usuario> usuarioMono =
                Mono.fromCallable(() -> Usuario.builder().nombre("Jorge").apellido("Cueva").build());

        Mono<Comentarios> comentariosMono = Mono.fromCallable(
                () -> Comentarios.builder()
                                 .comentario("Hola Jorge que tal")
                                 .comentario("Mañana tenemos reunion a primera hora")
                                 .comentario("Yo tambien estoy llevando el curso de Spring")
                                 .build());

        usuarioMono.zipWith(comentariosMono)
                   .map(tuple -> UsuarioComentario.builder().usuario(tuple.getT1()).comentarios(tuple.getT2()).build())
                   .subscribe(uc -> LOG.info(uc.toString()));
    }

    private void ejemploZipWithRange()
    {
        Flux.just(1, 2, 3, 4)
            .map(n -> n * 2)
            .zipWith(Flux.range(0, 4), (uno, dos) -> String.format("Primer Flux: %d, Segundo Flux: %d", uno, dos))
            .subscribe(nums -> LOG.info(nums));
    }

    private void ejemploInterval()
    {
        Flux<Integer> rango = Flux.range(1, 12);
        // crea un flujo desde 0, obteniendo el siguiente valor cada 2 segundos en paralelo
        Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));

        rango.zipWith(retraso, (ra, re) -> ra)
             .doOnNext(n -> LOG.info(n.toString()))
             //.subscribe() //Con un subscribe no se mostraria nada ya que todoo se esta ejecutando en segundo plano
             .blockLast(); //bloquea el thread principal (main), hasta que se haya emitido el ultimo elemento, lo ideal es no usar esto y que no sea bloqueante
    }

    private void ejemploDelayElements()
            throws InterruptedException
    {
        Flux<Integer> rango = Flux.range(1, 12)
                                  .delayElements(Duration.ofSeconds(2))
                                  .doOnNext(n -> LOG.info(n.toString()));
        rango.subscribe();

        // otra forma de pausar el thread principal para mostrar lo que los otros threads hacen
        Thread.sleep(12000);
    }

    private void ejemploIntervalInfinito()
            throws InterruptedException
    {
        CountDownLatch latch = new CountDownLatch(1);

        Flux.interval(Duration.ofSeconds(1))
            .flatMap(i -> {
                if (i > 5) {
                    return Flux.error(new InterruptedException("Solo hasta 5"));
                }
                return Flux.just(i);
            })
            .map(v -> "Hola" + v)
            .retry(2)
            //.doOnNext(LOG::info)
            .doOnTerminate(latch::countDown) // debe colocarse al ultimo luego de que el flujo final se haya obtenido
            .subscribe(LOG::info, e -> LOG.error(e.getMessage()));

        latch.await();
    }

    private void ejemploIntervalInfinitoDeCreate()
    {
        Flux.create(emitter -> {
            Timer timer = new Timer();
            timer.schedule(new TimerTask()
            {
                private Integer contador = 0;

                @Override
                public void run()
                {
                    emitter.next(++contador);
                    if (contador == 10) {
                        timer.cancel();
                        emitter.complete();
                    }
                    Random r = new Random();
                    if (contador == r.nextInt(10)) {
                        emitter.error(new InterruptedException("Error, el flux se ha detenido en" + contador));
                    }
                }
            }, 1000, 200);
        })
            //.doOnNext(next -> LOG.info(next.toString()))
            //.doOnComplete(() -> LOG.info("Terminamosssss"))
            .subscribe(next -> LOG.info(next.toString()),
                       error -> LOG.error(error.getMessage()),
                       () -> LOG.info("Terminamosssss"));
    }

    /**
     * Esto es para indicar un limite de elementos a recibir de una vez en el flujo, para evitar una sobrecarga.
     */
    private void ejemploContraPresion()
    {
        Flux.range(1, 10)
            .log()
            .limitRate(5)
            .subscribe(next -> LOG.info(next.toString())/*new Subscriber<Integer>()
            {
                private Subscription s;

                private Integer limite = 5;
                private Integer consumido = 0;

                @Override
                public void onSubscribe(Subscription s)
                {
                    this.s = s;
                    s.request(this.limite);
                }

                @Override
                public void onNext(Integer integer)
                {
                    LOG.info(integer.toString());
                    consumido++;
                    if (consumido == limite) {
                        consumido = 0;
                        s.request(limite);
                    }
                }

                @Override
                public void onError(Throwable t)
                {

                }

                @Override
                public void onComplete()
                {

                }
            }*/);
    }
}
