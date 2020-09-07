package cn.abelib.minebatis.builder;


/**
 * @author abel.huang
 * @date 2020/8/6 13:54
 */
public class MapperBuilderAssistant {
   private String currentNamespace;

    public MapperBuilderAssistant(){}

    public String getCurrentNamespace() {
        return currentNamespace;
    }

    public void setCurrentNamespace(String currentNamespace) {
        if (currentNamespace == null) {
            throw new RuntimeException("The mapper element requires a namespace attribute to be specified.");
        }

        if (this.currentNamespace != null && !this.currentNamespace.equals(currentNamespace)) {
            throw new RuntimeException("Wrong namespace. Expected '"
                    + this.currentNamespace + "' but found '" + currentNamespace + "'.");
        }

        this.currentNamespace = currentNamespace;
    }
}
