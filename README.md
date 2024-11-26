# Java 22 New Feature - Stream Gatherers


## Mas o que é Stream Gatherers afinal ? 
Este código tem como objetivo explicar esta nova funcionalidade mostrando como filtrar e agrupar uma lista de pessoas com base em seu tipo de classe (`ClassTypeEnum`). Utilizando conceitos de programação funcional e Streams do Java para tornar o processo eficiente e claro.

## Componentes Principais

1. **Classe `Main`**: Ponto de entrada do programa onde a execução começa.
2. **Lista de Pessoas**: Um conjunto de objetos `Person`, cada um com um nome, idade e tipo de classe.

### Métodos

#### `getGathererWithList()`
Este método cria um **Gatherer** que ajuda a agrupar elementos em listas. Aqui está o que acontece:

- **Inicialização**: Uma nova lista é criada para armazenar os elementos.
- **Integração**: Quando um elemento é adicionado, verificamos se a lista atingiu um determinado limite (`LIMIT`). Se sim, agrupamos os elementos e os enviamos para o próximo estágio do processamento. Se não, continuamos a adicionar elementos.
- **Finalização**: Após o processamento, se restarem elementos na lista que não foram agrupados, eles também são enviados.

Isso permite que o código agrupe as pessoas em sublistas com um número máximo de elementos, facilitando a manipulação posterior.

#### `getGathererWithMap(Function<T, R> mapper)`
Este método cria um **Gatherer** que transforma elementos de um tipo para outro.

- **Integração**: Cada elemento é passado por uma função de mapeamento que transforma o elemento original em um novo tipo. O elemento transformado é então enviado para o próximo estágio do processamento.

Esse método é útil quando precisamos alterar a forma dos dados antes de processá-los.

```java
// "Se o código já é simples e óbvio, não há necessidade de adicionar um comentário" -  ¯\_(ツ)_/¯ ( eu não ligo, segue código )

private static <T> Gatherer<T, List<T>, List<T>> getGathererWithList() {
    // Fornecedor que inicializa uma nova ArrayList
    Supplier<List<T>> initializer = ArrayList::new;
    
    // Integrador que adiciona elementos à lista e os organiza em grupos
    Gatherer.Integrator<List<T>, T, List<T>> integrator = (state, element, downstream) -> {
        state.add(element);
        
        // Verifica se o tamanho da lista atingiu o LIMIT definido
        if (state.size() == LIMIT) {
            
            // Cria uma cópia imutável do grupo atual
            var group = List.copyOf(state); 

            // Envia o grupo para o downstream
            downstream.push(group); 
            
            // Limpa o estado para o próximo grupo
            state.clear(); 
        }
        
        // Indica que o elemento foi processado com sucesso
        return true; 
    };

    // Finalizador para lidar com quaisquer elementos restantes na lista após o processamento
    BiConsumer<List<T>, Gatherer.Downstream<? super List<T>>> finisher = (state, downStream) -> {
        
        // Envia elementos restantes se a lista não estiver vazia
        if (!state.isEmpty()) {
            downStream.push(List.copyOf(state));
        }
    };

    // Retorna um Gatherer com um finalizador para garantir que todos os elementos sejam processados
    return Gatherer.ofSequential(initializer, integrator, finisher);
}

private static <T, R> Gatherer<T, ?, R> getGathererWithMap(Function<T, R> mapper) {
    // Integrador que mapeia elementos para um novo tipo e os envia para o downstream
    Gatherer.Integrator<Void, T, R> integrator = (_, element, downStream) -> {
        
        // Aplica a função de mapeamento
        R newElement = mapper.apply(element); 
        
        // Envia o elemento mapeado para o downstream
        downStream.push(newElement); 

        // Indica que o processamento foi bem-sucedido
        return true;
    };

    // Retorna um Gatherer que transforma elementos usando a função de mapeamento fornecida
    return Gatherer.of(integrator);
}
```

## Enfim...
Este código é um exemplo prático de como usar Streams e Gatherers em Java para manipular e organizar dados de forma eficiente. Nele você pode ver como as pessoas são filtradas e agrupadas com base em seu tipo de classe, mostrando a flexibilidade e o poder da programação funcional.